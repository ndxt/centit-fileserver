package com.centit.fileserver.config;

import com.centit.fileserver.fileaccess.AliyunOssStore;
import com.centit.fileserver.utils.FileStore;
import com.centit.fileserver.utils.OsFileStore;
import com.centit.framework.common.SysParametersUtils;
import com.centit.framework.components.impl.NotificationCenterImpl;
import com.centit.framework.components.impl.TextOperationLogWriterImpl;
import com.centit.framework.config.SpringSecurityDaoConfig;
import com.centit.framework.core.config.DataSourceConfig;
import com.centit.framework.ip.app.config.IPOrStaticAppSystemBeanConfig;
import com.centit.framework.jdbc.config.JdbcConfig;
import com.centit.framework.model.adapter.NotificationCenter;
import com.centit.framework.model.adapter.OperationLogWriter;
import com.centit.search.document.FileDocument;
import com.centit.search.service.Indexer;
import com.centit.search.service.IndexerSearcherFactory;
import com.centit.search.service.Searcher;
import com.centit.support.algorithm.BooleanBaseOpt;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

/**
 * Created by codefan on 17-7-18.
 */
@ComponentScan(basePackages = "com.centit",
        excludeFilters = @ComponentScan.Filter(value = org.springframework.stereotype.Controller.class))
@Import({SpringSecurityDaoConfig.class,
        IPOrStaticAppSystemBeanConfig.class,
        DataSourceConfig.class,
        JdbcConfig.class})
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class ServiceConfig {
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
            fs.setAccessKeyId(env.getProperty("oos.accessKeyId"));
            fs.setSecretAccessKey(env.getProperty("oos.secretAccessKey"));
            fs.setBucketName(env.getProperty("oos.bucketName"));
            return fs;
        }else /*if("os".equals(fileStoreType))*/{

            String baseHome = env.getProperty("os.file.base.dir");
            if(StringUtils.isBlank(baseHome)) {
                baseHome = env.getProperty("app.home") + "/upload";
            }
            return new OsFileStore(baseHome);
        }

    }

    @Bean
    public Indexer documentIndexer(){
        if(BooleanBaseOpt.castObjectToBoolean(
                env.getProperty("fulltext.index.enable"),false)) {
            return IndexerSearcherFactory.obtainIndexer(
                    IndexerSearcherFactory.loadESServerConfigFormProperties(
                            SysParametersUtils.loadProperties()), FileDocument.class);
        }
        return null;
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
        operationLog.init();
        return operationLog;
    }

    @Bean
    public InstantiationServiceBeanPostProcessor instantiationServiceBeanPostProcessor() {
        return new InstantiationServiceBeanPostProcessor();
    }
}

