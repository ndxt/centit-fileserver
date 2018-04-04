package com.centit.fileserver.demo.config;

import com.centit.fileserver.utils.FileStore;
import com.centit.fileserver.utils.OsFileStore;
import com.centit.framework.common.SysParametersUtils;
import com.centit.framework.components.impl.NotificationCenterImpl;
import com.centit.framework.components.impl.TextOperationLogWriterImpl;
import com.centit.framework.config.H2SessionPersistenceConfig;
import com.centit.framework.config.RedisSessionPersistenceConfig;
import com.centit.framework.model.adapter.NotificationCenter;
import com.centit.framework.model.adapter.OperationLogWriter;
import com.centit.framework.config.SpringSecurityCasConfig;
import com.centit.framework.config.SpringSecurityDaoConfig;
import com.centit.framework.staticsystem.config.StaticSystemBeanConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

/**
 * Created by codefan on 17-7-18.
 */
@Configuration
@ComponentScan(basePackages = "com.centit",
        excludeFilters = @ComponentScan.Filter(value = org.springframework.stereotype.Controller.class))
@Import({RedisSessionPersistenceConfig.class,
        H2SessionPersistenceConfig.class,
        SpringSecurityDaoConfig.class,
        SpringSecurityCasConfig.class,
        StaticSystemBeanConfig.class})
public class ServiceConfig {

    @Autowired
    private Environment env;

    @Bean
    public FileStore fileStore(){

        String baseHome = env.getProperty("os.file.base.dir");
        if(StringUtils.isBlank(baseHome)) {
            baseHome = SysParametersUtils.getUploadHome();
        }

        return new OsFileStore(baseHome);
    }


    @Bean
    public NotificationCenter notificationCenter() {
        NotificationCenterImpl notificationCenter = new NotificationCenterImpl();
        notificationCenter.initMsgSenders();
        //notificationCenter.registerMessageSender("innerMsg",innerMessageManager);
        return notificationCenter;
    }

    @Bean
    @Lazy(value = false)
    public OperationLogWriter operationLogWriter() {
        TextOperationLogWriterImpl  operationLog =  new TextOperationLogWriterImpl();
        operationLog.init();
        return operationLog;
    }

 }
