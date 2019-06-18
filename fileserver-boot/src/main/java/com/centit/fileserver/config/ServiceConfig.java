package com.centit.fileserver.config;

import com.centit.fileserver.store.plugin.AliyunOssStore;
import com.centit.fileserver.store.plugin.TxyunCosStore;
import com.centit.fileserver.utils.FileStore;
import com.centit.fileserver.utils.OsFileStore;
import com.centit.framework.components.impl.NotificationCenterImpl;
import com.centit.framework.components.impl.TextOperationLogWriterImpl;
import com.centit.framework.model.adapter.NotificationCenter;
import com.centit.framework.model.adapter.OperationLogWriter;
import com.centit.search.document.FileDocument;
import com.centit.search.service.Indexer;
import com.centit.search.service.IndexerSearcherFactory;
import com.centit.search.service.Searcher;
import com.centit.support.algorithm.BooleanBaseOpt;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

/**
 * Created by codefan on 17-7-18.
 */
@Configuration
@ComponentScan(basePackages = "com.centit",
        excludeFilters = @ComponentScan.Filter(value = org.springframework.stereotype.Controller.class))
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableConfigurationProperties(FileServerProperties.class)
public class ServiceConfig {

    @Value("${framework.app.home:/}")
    protected String appHome;
    /* @Bean
    @Lazy(value = false)
    public IntegrationEnvironment integrationEnvironment() {
        return new DummyIntegrationEnvironment();
    }*/

    @Autowired
    FileServerProperties fileServerProperties;

//    @Bean
//    public FileStore fileStore(){
//        String fileStoreType= fileServerProperties.getFileStore().getType();
//
//        if("oss".equals(fileStoreType)){//ali-oss
//            AliyunOssStore fs = new AliyunOssStore();
//            fs.setEndPoint(fileServerProperties.getFileStore().getOss().getEndPoint());
//            fs.setAccessKeyId(fileServerProperties.getFileStore().getOss().getAccessKeyId());
//            fs.setSecretAccessKey(fileServerProperties.getFileStore().getOss().getSecretAccessKey());
//            fs.setBucketName(fileServerProperties.getFileStore().getOss().getBucketName());
//            return fs;
//        }else /*if("os".equals(fileStoreType))*/{
//
//            String baseHome = fileServerProperties.getFileStore().getOs().getBaseDir();
//            if(StringUtils.isBlank(baseHome)) {
//                baseHome = appHome + "/upload";
//            }
//            return new OsFileStore(baseHome);
//        }
//
//    }

    @Bean
    @ConditionalOnProperty(prefix = "fileserver.file-store.os", name = "enabled")
    public FileStore osFileStore() {
        String baseHome = fileServerProperties.getFileStore().getOs().getBaseDir();
        if (StringUtils.isBlank(baseHome)) {
            baseHome = appHome + "/upload";
        }
        return new OsFileStore(baseHome);
    }

    @Bean
    @ConditionalOnProperty(prefix = "fileserver.file-store.oss", name = "enabled")
    public FileStore ossFileStore() {
        AliyunOssStore fs = new AliyunOssStore();
        fs.setEndPoint(fileServerProperties.getFileStore().getOss().getEndPoint());
        fs.setAccessKeyId(fileServerProperties.getFileStore().getOss().getAccessKeyId());
        fs.setSecretAccessKey(fileServerProperties.getFileStore().getOss().getSecretAccessKey());
        fs.setBucketName(fileServerProperties.getFileStore().getOss().getBucketName());
        return fs;
    }

    @Bean
    @ConditionalOnProperty(prefix = "fileserver.file-store.cos", name = "enabled")
    public FileStore cosFileStore() {
        TxyunCosStore cosStore = new TxyunCosStore();
        cosStore.setRegion(fileServerProperties.getFileStore().getCos().getRegion());
        cosStore.setAppId(fileServerProperties.getFileStore().getCos().getAppId());
        cosStore.setSecretId(fileServerProperties.getFileStore().getCos().getSecretId());
        cosStore.setSecretKey(fileServerProperties.getFileStore().getCos().getSecretKey());
        cosStore.setBucketName(fileServerProperties.getFileStore().getCos().getBucketName());
        return cosStore;
    }


    @Bean
    public Indexer documentIndexer(){
        if(BooleanBaseOpt.castObjectToBoolean(
                fileServerProperties.isFulltextIndexEnable(),false)) {
            return IndexerSearcherFactory.obtainIndexer(
                fileServerProperties.getElasticSearch(), FileDocument.class);
        }
        return null;
    }

    @Bean
    public Searcher documentSearcher(){
        if(BooleanBaseOpt.castObjectToBoolean(
            fileServerProperties.isFulltextIndexEnable(),false)) {
            return IndexerSearcherFactory.obtainSearcher(
                    fileServerProperties.getElasticSearch(), FileDocument.class);
        }
        return null;
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

//    @Bean
//    public CsrfTokenRepository csrfTokenRepository() {
//        return new HttpSessionCsrfTokenRepository();
//    }

}

