package com.centit.fileserver.config;

import com.centit.fileserver.common.FileTaskInfo;
import com.centit.fileserver.common.FileTaskQueue;
import com.centit.fileserver.common.FileStore;
import com.centit.fileserver.store.plugin.AliyunOssStore;
import com.centit.fileserver.store.plugin.TxyunCosStore;
import com.centit.fileserver.task.*;
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
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Bean
    //@ConditionalOnProperty(prefix = "fileserver.file-store.os", name = "enabled")
    public FileStore fileStore() {
        if("oss".equalsIgnoreCase(fileServerProperties.getFileStore().getStoreType())){
            AliyunOssStore fs = new AliyunOssStore();
            fs.setEndPoint(fileServerProperties.getFileStore().getOss().getEndPoint());
            fs.setAccessKeyId(fileServerProperties.getFileStore().getOss().getAccessKeyId());
            fs.setSecretAccessKey(fileServerProperties.getFileStore().getOss().getSecretAccessKey());
            fs.setBucketName(fileServerProperties.getFileStore().getOss().getBucketName());
            return fs;
        } else if("cos".equalsIgnoreCase(fileServerProperties.getFileStore().getStoreType())){
            TxyunCosStore cosStore = new TxyunCosStore();
            cosStore.setRegion(fileServerProperties.getFileStore().getCos().getRegion());
            cosStore.setAppId(fileServerProperties.getFileStore().getCos().getAppId());
            cosStore.setSecretId(fileServerProperties.getFileStore().getCos().getSecretId());
            cosStore.setSecretKey(fileServerProperties.getFileStore().getCos().getSecretKey());
            cosStore.setBucketName(fileServerProperties.getFileStore().getCos().getBucketName());
            return cosStore;
        } else {//if("os".equalsIgnoreCase(fileServerProperties.getFileStore().getStoreType())) {
            String baseHome = fileServerProperties.getFileStore().getOs().getBaseDir();
            if (StringUtils.isBlank(baseHome)) {
                baseHome = appHome + "/upload";
            }
            return new OsFileStore(baseHome);
        }
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
    public FileTaskQueue fileOptTaskQueue() throws Exception {
        return new LinkedBlockingQueueFileOptTaskQueue(appHome + "/task");
    }
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
    public InstantiationServiceBeanPostProcessor instantiationServiceBeanPostProcessor() {
        return new InstantiationServiceBeanPostProcessor();
    }


}

