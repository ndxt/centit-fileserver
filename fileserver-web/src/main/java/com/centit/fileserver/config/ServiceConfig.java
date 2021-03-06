package com.centit.fileserver.config;

import com.centit.fileserver.common.FileStore;
import com.centit.fileserver.common.FileTaskQueue;
import com.centit.fileserver.store.plugin.AliyunOssStore;
import com.centit.fileserver.store.plugin.TxyunCosStore;
import com.centit.fileserver.task.*;
import com.centit.fileserver.utils.OsFileStore;
import com.centit.framework.common.SysParametersUtils;
import com.centit.framework.components.impl.NotificationCenterImpl;
import com.centit.framework.components.impl.TextOperationLogWriterImpl;
import com.centit.framework.config.SpringSecurityCasConfig;
import com.centit.framework.config.SpringSecurityDaoConfig;
import com.centit.framework.ip.app.config.IPOrStaticAppSystemBeanConfig;
import com.centit.framework.jdbc.config.JdbcConfig;
import com.centit.framework.model.adapter.NotificationCenter;
import com.centit.framework.model.adapter.OperationLogWriter;
import com.centit.framework.security.model.StandardPasswordEncoderImpl;
import com.centit.search.document.FileDocument;
import com.centit.search.document.ObjectDocument;
import com.centit.search.service.ESServerConfig;
import com.centit.search.service.Impl.ESIndexer;
import com.centit.search.service.Impl.ESSearcher;
import com.centit.search.service.IndexerSearcherFactory;
import com.centit.search.service.Searcher;
import com.centit.support.algorithm.BooleanBaseOpt;
import com.centit.support.security.AESSecurityUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by codefan on 17-7-18.
 */
@ComponentScan(basePackages = "com.centit",
        excludeFilters = @ComponentScan.Filter(value = org.springframework.stereotype.Controller.class))
@Import({
        SpringSecurityDaoConfig.class,
        SpringSecurityCasConfig.class,
        //StaticSystemBeanConfig.class,
        //SystemBeanConfig.class,
        IPOrStaticAppSystemBeanConfig.class,
        JdbcConfig.class})
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableSpringHttpSession
public class ServiceConfig {

    @Value("${app.home:./}")
    private String appHome;
    /* @Bean
    @Lazy(value = false)
    public IntegrationEnvironment integrationEnvironment() {
        return new DummyIntegrationEnvironment();
    }*/
    @Autowired
    private Environment env;


    @Bean
    public FileStore fileStore(){
        String fileStoreType= env.getProperty("filestore.type","os");

        if("oss".equals(fileStoreType)){//ali-oss
            AliyunOssStore fs = new AliyunOssStore();
            fs.setEndPoint(env.getProperty("oos.endPoint"));
            fs.setAccessKeyId(
                AESSecurityUtils.decryptParameterString(env.getProperty("oos.accessKeyId")));
            fs.setSecretAccessKey(
                AESSecurityUtils.decryptParameterString(env.getProperty("oos.secretAccessKey")));
            fs.setBucketName(env.getProperty("oos.bucketName"));
            return fs;
        }else if("cos".equals(fileStoreType)){
            TxyunCosStore cosStore = new TxyunCosStore();
            cosStore.setRegion(env.getProperty("cos.region"));
            cosStore.setAppId(env.getProperty("cos.appId"));
            cosStore.setSecretId(
                AESSecurityUtils.decryptParameterString(env.getProperty("cos.secretId")));
            cosStore.setSecretKey(
                AESSecurityUtils.decryptParameterString(env.getProperty("cos.secretKey")));
            cosStore.setBucketName(env.getProperty("cos.bucketName"));
            return cosStore;
        }else {
            String baseHome = env.getProperty("os.file.base.dir");
            if (StringUtils.isBlank(baseHome)) {
                baseHome = env.getProperty("app.home") + "/upload";
            }
            return new OsFileStore(baseHome);
        }
    }

    @Bean
    public FileTaskQueue fileOptTaskQueue() throws Exception {
        return new LinkedBlockingQueueFileOptTaskQueue(appHome + "/task");
    }

    /*@Bean
    public FileOptTaskQueue fileOptTaskQueue() {
        RedisFileOptTaskQueue taskQueue = new RedisFileOptTaskQueue();
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(env.getProperty("file.redis.host","127.0.0.1"));
        configuration.setPort(env.getProperty("file.redis.port", Integer.class, 6379));
        configuration.setDatabase(1);
        JedisConnectionFactory factory = new JedisConnectionFactory(configuration);
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.afterPropertiesSet();

        taskQueue.setRedisTemplate(redisTemplate);
        return taskQueue;
    }*/

    /* 这个定时任务 不能用run来做，应该用一个 定时任务容器
     */
    @Bean
    public FileOptTaskExecutor fileOptTaskExecutor(
                                        @Autowired FileTaskQueue fileOptTaskQueue,
                                        @Autowired SaveFileOpt saveFileOpt,
                                        @Autowired CreatePdfOpt createPdfOpt,
                                        @Autowired PdfWatermarkOpt pdfWatermarkOpt,
                                        @Autowired AddThumbnailOpt addThumbnailOpt,
                                        @Autowired ZipFileOpt zipFileOpt,
                                        @Autowired EncryptFileWithAesOpt encryptFileWithAesOpt,
                                        @Autowired DocumentIndexOpt documentIndexOpt) {
        FileOptTaskExecutor fileOptTaskExecutor = new FileOptTaskExecutor();
        fileOptTaskExecutor.setFileOptTaskQueue(fileOptTaskQueue);

        fileOptTaskExecutor.addFileOperator(saveFileOpt);
        fileOptTaskExecutor.addFileOperator(createPdfOpt);
        fileOptTaskExecutor.addFileOperator(pdfWatermarkOpt);
        fileOptTaskExecutor.addFileOperator(addThumbnailOpt);
        fileOptTaskExecutor.addFileOperator(zipFileOpt);
        fileOptTaskExecutor.addFileOperator(encryptFileWithAesOpt);
        fileOptTaskExecutor.addFileOperator(documentIndexOpt);
        return fileOptTaskExecutor;
    }

    @Bean
    public SchedulerFactory schedulerFactory()  {
        return new StdSchedulerFactory();
    }

    @Bean
    public ESServerConfig esServerConfig(){
        return IndexerSearcherFactory.loadESServerConfigFormProperties(
            SysParametersUtils.loadProperties()
        );
    }

    @Bean(name = "esObjectIndexer")
    public ESIndexer esObjectIndexer(@Autowired ESServerConfig esServerConfig){
        return IndexerSearcherFactory.obtainIndexer(
            esServerConfig, ObjectDocument.class);
    }

    @Bean
    public Searcher documentSearcher(){
        if(BooleanBaseOpt.castObjectToBoolean(
                env.getProperty("fulltext.index.enable"),false)) {
            return IndexerSearcherFactory.obtainSearcher(
                    IndexerSearcherFactory.loadESServerConfigFormProperties(
                            SysParametersUtils.loadProperties()), FileDocument.class);
        }
        return null;
    }
    @Bean(name = "esObjectSearcher")
    public ESSearcher esObjectSearcher(@Autowired ESServerConfig esServerConfig){
        return IndexerSearcherFactory.obtainSearcher(
            esServerConfig, ObjectDocument.class);
    }
    @Bean
    public NotificationCenter notificationCenter() {
        NotificationCenterImpl notificationCenter = new NotificationCenterImpl();
        notificationCenter.initDummyMsgSenders();
        //notificationCenter.registerMessageSender("innerMsg",innerMessageManager);
        return notificationCenter;
    }

    @Bean
    @Lazy(value = false)
    public OperationLogWriter operationLogWriter() {
        TextOperationLogWriterImpl operationLog = new TextOperationLogWriterImpl();
        operationLog.setOptLogHomePath(appHome+"/logs");
        operationLog.init();
        return operationLog;
    }

    @Bean
    public InstantiationServiceBeanPostProcessor instantiationServiceBeanPostProcessor() {
        return new InstantiationServiceBeanPostProcessor();
    }

    /**
     * 这个bean必须要有
     * @return CentitPasswordEncoder 密码加密算法
     */
    @Bean("passwordEncoder")
    public StandardPasswordEncoderImpl passwordEncoder() {
        return  new StandardPasswordEncoderImpl();
    }
    //这个bean必须要有 可以配置不同策略的session保存方案

    @Bean
    public MapSessionRepository sessionRepository() {
        return new MapSessionRepository(new ConcurrentHashMap<>());
    }
    /*
    @Bean
    public FindByIndexNameSessionRepository sessionRepository() {
        return new SimpleMapSessionRepository();
    }

    @Bean
    public SessionRegistry sessionRegistry(
        @Autowired FindByIndexNameSessionRepository sessionRepository){
        return new SpringSessionBackedSessionRegistry(sessionRepository);
    }*/
}

