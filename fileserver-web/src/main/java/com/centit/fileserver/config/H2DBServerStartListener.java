package com.centit.fileserver.config;

import com.centit.framework.common.SysParametersUtils;
import com.centit.support.algorithm.StringRegularOpt;
import com.centit.support.file.FileSystemOpt;
import com.centit.support.file.PropertiesReader;
import org.apache.commons.lang3.StringUtils;
import org.h2.tools.Server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * Created by zou_wy on 2017/7/13.
 */
public class H2DBServerStartListener implements ServletContextListener {

    /*
     * Web应用初始化时启动H2数据库
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Properties properties = PropertiesReader.getClassPathProperties("/system.properties");
        String jdbcUrl = properties.getProperty("jdbc.url");

        if(jdbcUrl.startsWith("jdbc:h2")){
            //jdbc:h2:file:/D/Projects/RunData/file_home/config/db;
            int bPos = jdbcUrl.indexOf("file");
            bPos = jdbcUrl.indexOf(':',bPos)+1;
            int ePos = jdbcUrl.indexOf(';',bPos);
            String dbFile = ePos<1 ? jdbcUrl.substring(bPos) : jdbcUrl.substring(bPos,ePos);
            //数据文件不存在就初始化数据库
            if (!FileSystemOpt.existFile(dbFile.trim())) {
                try {
                    Class.forName( properties.getProperty("jdbc.driver"));//  "org.h2.Driver");
                    DriverManager.getConnection( jdbcUrl +
                            ";INIT=RUNSCRIPT FROM 'classpath:h2.sql'", "sa", "sa");
                } catch (Exception e) {
                    //throw new RuntimeException(e);
                }
            }
        }
    }

    /*
     * Web应用销毁时停止H2数据库
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {

        /*Properties properties = PropertiesReader.getClassPathProperties("/system.properties");
        String jdbcUrl = properties.getProperty("jdbc.url");

        if(jdbcUrl.startsWith("jdbc:h2")){
            DriverManager.deregisterDriver(properties.getProperty("jdbc.driver"));
        }*/
    }
}
