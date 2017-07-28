package com.centit.fileserver.config;

import com.centit.framework.config.SystemSpringMvcConfig;
import com.centit.framework.config.WebConfig;
import com.centit.support.file.FileSystemOpt;
import com.centit.support.file.PropertiesReader;
import org.h2.server.web.WebServlet;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * Created by zou_wy on 2017/3/29.
 */

public class WebInitializer implements WebApplicationInitializer {


    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {

        initializeSpringConfig(servletContext);
        initializeSystemSpringMvcConfig(servletContext);
        initializeSpringMvcConfig(servletContext);
        WebConfig.registerRequestContextListener(servletContext);
        WebConfig.registerSingleSignOutHttpSessionListener(servletContext);
        WebConfig.registerResponseCorsFilter(servletContext);
        WebConfig.registerCharacterEncodingFilter(servletContext);
        WebConfig.registerHttpPutFormContentFilter(servletContext);
        WebConfig.registerHiddenHttpMethodFilter(servletContext);
        WebConfig.registerRequestThreadLocalFilter(servletContext);
        WebConfig.registerSpringSecurityFilter(servletContext);

        Properties properties = PropertiesReader.getClassPathProperties("/system.properties");
        String jdbcUrl = properties.getProperty("jdbc.url");

        if(jdbcUrl.startsWith("jdbc:h2")){
            initializeH2Console(servletContext);
        }
    }

    /**
     * 加载Spring 配置
     * @param servletContext ServletContext
     */
    private void initializeSpringConfig(ServletContext servletContext){
        AnnotationConfigWebApplicationContext springContext = new AnnotationConfigWebApplicationContext();
        springContext.register(ServiceConfig.class);
        servletContext.addListener(new ContextLoaderListener(springContext));
    }

    /**
     * 加载Servlet 配置
     * @param servletContext ServletContext
     */
    private void initializeSystemSpringMvcConfig(ServletContext servletContext) {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(SystemSpringMvcConfig.class);
        ServletRegistration.Dynamic system  = servletContext.addServlet("system", new DispatcherServlet(context));
        system.addMapping("/system/*");
        system.setLoadOnStartup(1);
        system.setAsyncSupported(true);
    }

    /**
     * 加载Servlet 项目配置
     * @param servletContext ServletContext
     */
    private void initializeSpringMvcConfig(ServletContext servletContext) {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(NormalSpringMvcConfig.class);
        ServletRegistration.Dynamic system  = servletContext.addServlet("service", new DispatcherServlet(context));
        system.addMapping("/service/*");
        system.setLoadOnStartup(1);
        system.setAsyncSupported(true);
    }


    /**
     * 访问 h2 console
     * @param servletContext ServletContext
     */
    private void initializeH2Console(ServletContext servletContext){

        /*Properties properties = PropertiesReader.getClassPathProperties("/system.properties");
        String jdbcUrl = properties.getProperty("jdbc.url");
        int bPos = jdbcUrl.indexOf("file");
        bPos = jdbcUrl.indexOf(':',bPos)+1;
        int ePos = jdbcUrl.indexOf(';',bPos);
        String dbFile = ePos<1 ? jdbcUrl.substring(bPos) : jdbcUrl.substring(bPos,ePos);
        //数据文件不存在就初始化数据库
        if (!FileSystemOpt.existFile(dbFile.trim())) {
            try {
                Class.forName( properties.getProperty("jdbc.driver"));//  "org.h2.Driver");
                DriverManager.getConnection( jdbcUrl +
                        ";INIT=RUNSCRIPT FROM 'classpath:db/migration/h2/V1_1__system-h2.sql'", "sa", "sa");
            } catch (Exception e) {
                //throw new RuntimeException(e);
            }
        }*/

        AnnotationConfigWebApplicationContext contextSer = new AnnotationConfigWebApplicationContext();
        contextSer.register(NormalSpringMvcConfig.class);
        contextSer.setServletContext(servletContext);
        ServletRegistration.Dynamic h2console  = servletContext.addServlet("h2console", WebServlet.class);
        h2console.setInitParameter("webAllowOthers", "");
        h2console.addMapping("/console/*");
        h2console.setLoadOnStartup(1);
        h2console.setAsyncSupported(true);
    }


}
