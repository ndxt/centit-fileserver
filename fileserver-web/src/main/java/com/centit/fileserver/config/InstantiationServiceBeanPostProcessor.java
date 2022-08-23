package com.centit.fileserver.config;

import com.centit.fileserver.task.FileOptTaskExecutor;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.framework.common.SysParametersUtils;
import com.centit.framework.components.CodeRepositoryCache;
import com.centit.framework.components.OperationLogCenter;
import com.centit.framework.model.adapter.MessageSender;
import com.centit.framework.model.adapter.NotificationCenter;
import com.centit.framework.model.adapter.OperationLogWriter;
import com.centit.framework.model.adapter.PlatformEnvironment;
import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.quartz.JavaBeanJob;
import com.centit.support.quartz.QuartzJobUtils;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.io.File;

/**
 * Created by codefan on 17-7-6.
 */
public class InstantiationServiceBeanPostProcessor implements ApplicationListener<ContextRefreshedEvent>
{

    @Autowired
    protected NotificationCenter notificationCenter;

    @Autowired
    private OperationLogWriter optLogManager;

    @Autowired(required = false)
    private MessageSender innerMessageManager;

    @Autowired
    protected PlatformEnvironment platformEnvironment;

    @Autowired
    protected FileOptTaskExecutor fileOptTaskExecutor;

    @Autowired
    protected SchedulerFactory schedulerFactory;
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {


        CodeRepositoryCache.setPlatformEnvironment(platformEnvironment);

        if (innerMessageManager != null) {
            notificationCenter.registerMessageSender("innerMsg", innerMessageManager);
            notificationCenter.appointDefaultSendType("innerMsg");
        }
        if (optLogManager != null) {
            OperationLogCenter.registerOperationLogWriter(optLogManager);
        }
        // 创建定时任务
        try {
            Scheduler scheduler = schedulerFactory.getScheduler();
            QuartzJobUtils.registerJobType("bean", JavaBeanJob.class);
            QuartzJobUtils.createOrReplaceSimpleJob(scheduler, "fileOptJob",
                "default", "bean", 300,
                CollectionsOpt.createHashMap("bean", fileOptTaskExecutor,
                    "beanName", "fileOptTaskExecutor",
                    "methodName", "doFileOptJob"));
            scheduler.start();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}
