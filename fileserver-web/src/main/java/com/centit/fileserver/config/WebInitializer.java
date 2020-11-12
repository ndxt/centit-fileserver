package com.centit.fileserver.config;

import com.centit.framework.config.SystemSpringMvcConfig;
import com.centit.framework.config.WebConfig;
import com.centit.support.file.PropertiesReader;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.util.Properties;

/**
 * Created by zou_wy on 2017/3/29.
 */
public class WebInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        initializeSpringConfig(servletContext);
        String [] servletUrlPatterns = {"/system/*","/fileserver/*"};
        registerServletConfig(servletContext, "system",
            "/system/*",
            SystemSpringMvcConfig.class);
        registerServletConfig(servletContext, "fileserver",
            "/fileserver/*",
            NormalSpringMvcConfig.class);
        WebConfig.registerRequestContextListener(servletContext);
        WebConfig.registerSingleSignOutHttpSessionListener(servletContext);
        //WebConfig.registerResponseCorsFilter(servletContext);
        WebConfig.registerCharacterEncodingFilter(servletContext,servletUrlPatterns);
        WebConfig.registerHttpPutFormContentFilter(servletContext,servletUrlPatterns);
        WebConfig.registerHiddenHttpMethodFilter(servletContext,servletUrlPatterns);
        WebConfig.registerRequestThreadLocalFilter(servletContext);
        WebConfig.registerSpringSecurityFilter(servletContext,servletUrlPatterns);

        Properties properties = PropertiesReader.getClassPathProperties("/system.properties");
        String jdbcUrl = properties.getProperty("jdbc.url");

        if(jdbcUrl.startsWith("jdbc:h2")){
            WebConfig.initializeH2Console(servletContext);
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


    public static ServletRegistration.Dynamic registerServletConfig(ServletContext servletContext,
                                                                    String servletName, String servletUrlPattern,
                                                                    Class<?>... annotatedClasses ) {
        AnnotationConfigWebApplicationContext contextSer = new AnnotationConfigWebApplicationContext();
        contextSer.register(annotatedClasses);
        ServletRegistration.Dynamic servlet  = servletContext.addServlet(servletName,
            new DispatcherServlet(contextSer));
        servlet.addMapping(servletUrlPattern);
        servlet.setLoadOnStartup(1);
        servlet.setAsyncSupported(true);
        return servlet;
    }
 }
