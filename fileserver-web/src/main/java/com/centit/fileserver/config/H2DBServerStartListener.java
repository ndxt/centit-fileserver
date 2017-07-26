package com.centit.fileserver.config;

import com.centit.framework.common.SysParametersUtils;
import com.centit.support.algorithm.StringRegularOpt;
import com.centit.support.file.FileSystemOpt;
import com.centit.support.file.PropertiesReader;
import org.h2.tools.Server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.DriverManager;

/**
 * Created by zou_wy on 2017/7/13.
 */
public class H2DBServerStartListener implements ServletContextListener {

    //H2数据库服务器启动实例
    private Server server;
    /*
     * Web应用初始化时启动H2数据库
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {

        if( StringRegularOpt.isTrue(
                PropertiesReader.getClassPathProperties(
                        "/system.properties", "h2.enable"))) {

            String dbFile = SysParametersUtils.getConfigHome() + "\\db.mv.db";
            String jdbcUrl = SysParametersUtils.getConfigHome() + "\\db";
            if (!FileSystemOpt.existFile(dbFile)) {
                try {
                    Class.forName("org.h2.Driver");
                    DriverManager.getConnection("jdbc:h2:" + jdbcUrl + ";INIT=RUNSCRIPT FROM 'classpath:h2.sql'", "sa", "sa");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /*
     * Web应用销毁时停止H2数据库
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (this.server != null) {
            // 停止H2数据库
            this.server.stop();
            this.server = null;
        }
    }
}
