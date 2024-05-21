package com.centit.fileserver.config;

import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.spring.context.annotation.config.EnableNacosConfig;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import com.centit.fileserver.common.FileStore;
import com.centit.fileserver.common.FileTaskQueue;
import com.centit.fileserver.store.plugin.AliyunOssStore;
import com.centit.fileserver.store.plugin.TxyunCosStore;
import com.centit.fileserver.task.*;
import com.centit.fileserver.utils.OsFileStore;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.framework.components.impl.NotificationCenterImpl;
import com.centit.framework.config.SpringSecurityCasConfig;
import com.centit.framework.config.SpringSecurityDaoConfig;
import com.centit.framework.dubbo.config.DubboConfig;
import com.centit.framework.dubbo.config.IpServerDubboClientConfig;
import com.centit.framework.jdbc.config.JdbcConfig;
import com.centit.framework.model.adapter.NotificationCenter;
import com.centit.framework.model.adapter.OperationLogWriter;
import com.centit.framework.security.StandardPasswordEncoderImpl;
import com.centit.framework.system.service.ElkOptLogManager;
import com.centit.search.service.ESServerConfig;
import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.security.SecurityOptUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.io.File;

/**
 * Created by codefan on 17-7-18.
 */
@Configuration
@PropertySource("classpath:system.properties")
@ComponentScan(basePackages = "com.centit",
        excludeFilters = @ComponentScan.Filter(value = org.springframework.stereotype.Controller.class))
@Import({
        DubboConfig.class,
        IpServerDubboClientConfig.class,
        SpringSecurityDaoConfig.class,
        SpringSecurityCasConfig.class,
        JdbcConfig.class})
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableNacosConfig(globalProperties = @NacosProperties(serverAddr = "${nacos.server-addr}"))
@NacosPropertySource(dataId = "${nacos.system-dataid}",groupId = "CENTIT", autoRefreshed = true)
public class ServiceConfig implements EnvironmentAware {

    private Environment env;

    @Override
    public void setEnvironment(@Autowired Environment environment) {
        if (environment != null) {
            this.env = environment;
        }
    }

    @Bean
    public AutowiredAnnotationBeanPostProcessor autowiredAnnotationBeanPostProcessor(){
        return new AutowiredAnnotationBeanPostProcessor();
    }

    @Bean
    public FileStore fileStore(){
        SystemTempFileUtils.setTempFileDirectory(
            env.getProperty("app.home", ".") +File.separatorChar+ "temp" + File.separatorChar);
        String fileStoreType= env.getProperty("filestore.type","os");

        if("oss".equals(fileStoreType)){//ali-oss
            AliyunOssStore fs = new AliyunOssStore();
            fs.setEndPoint(env.getProperty("oos.endPoint"));
            fs.setAccessKeyId(
                SecurityOptUtils.decodeSecurityString(env.getProperty("oos.accessKeyId")));
            fs.setSecretAccessKey(
                SecurityOptUtils.decodeSecurityString(env.getProperty("oos.secretAccessKey")));
            fs.setBucketName(env.getProperty("oos.bucketName"));
            return fs;
        }else if("cos".equals(fileStoreType)){
            TxyunCosStore cosStore = new TxyunCosStore();
            cosStore.setRegion(env.getProperty("cos.region"));
            cosStore.setAppId(env.getProperty("cos.appId"));
            cosStore.setSecretId(
                SecurityOptUtils.decodeSecurityString(env.getProperty("cos.secretId")));
            cosStore.setSecretKey(
                SecurityOptUtils.decodeSecurityString(env.getProperty("cos.secretKey")));
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
        return new LinkedBlockingQueueFileOptTaskQueue(env.getProperty("app.home", ".") + "/task");
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
                                        @Autowired EncryptFileOpt encryptFileOpt,
                                        @Autowired DocumentIndexOpt documentIndexOpt) {

        FileOptTaskExecutor fileOptTaskExecutor = new FileOptTaskExecutor(saveFileOpt);

        fileOptTaskExecutor.setFileOptTaskQueue(fileOptTaskQueue);
        fileOptTaskExecutor.addFileOperator(saveFileOpt);
        fileOptTaskExecutor.addFileOperator(createPdfOpt);
        fileOptTaskExecutor.addFileOperator(pdfWatermarkOpt);
        fileOptTaskExecutor.addFileOperator(addThumbnailOpt);
        fileOptTaskExecutor.addFileOperator(zipFileOpt);
        fileOptTaskExecutor.addFileOperator(encryptFileOpt);
        fileOptTaskExecutor.addFileOperator(documentIndexOpt);
        return fileOptTaskExecutor;
    }

    @Bean
    public ESServerConfig esServerConfig() {
        ESServerConfig config = new ESServerConfig();
        config.setServerHostIp(env.getProperty("elasticsearch.server.ip"));
        config.setServerHostPort(env.getProperty("elasticsearch.server.port"));
        config.setClusterName(env.getProperty("elasticsearch.server.cluster"));
        config.setUsername(env.getProperty("elasticsearch.server.username"));
        config.setPassword(env.getProperty("elasticsearch.server.password"));
        config.setOsId(env.getProperty("elasticsearch.osId"));
        config.setMinScore(NumberBaseOpt.parseFloat(env.getProperty("elasticsearch.filter.minScore"), 0.5f));
        return config;
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
    public OperationLogWriter optLogManager(@Autowired ESServerConfig esServerConfig) {
        ElkOptLogManager operationLog = new ElkOptLogManager(esServerConfig);
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
    public CsrfTokenRepository csrfTokenRepository() {
        return new HttpSessionCsrfTokenRepository();
    }

    @Bean
    MessageSource messageSource() {
        ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
        ms.setBasename("classpath:i18n/messages");
        ms.setDefaultEncoding("UTF-8");
        return ms;
    }

    @Bean
    public LocalValidatorFactoryBean validatorFactory() {
        return new LocalValidatorFactoryBean();
    }
}

