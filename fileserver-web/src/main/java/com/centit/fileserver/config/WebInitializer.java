package com.centit.fileserver.config;

import com.centit.framework.config.SystemSpringMvcConfig;
import com.centit.framework.config.WebConfig;
import com.centit.support.file.PropertiesReader;
import org.apache.dubbo.remoting.http.servlet.DispatcherServlet;
import org.springframework.web.WebApplicationInitializer;

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
        String [] servletUrlPatterns = {"/system/*","/fileserver/*"};
        WebConfig.registerSpringConfig(servletContext, ServiceConfig.class);
        WebConfig.registerServletConfig(servletContext, "system",
            "/system/*",
            SystemSpringMvcConfig.class,SwaggerConfig.class);
        WebConfig.registerServletConfig(servletContext, "fileserver",
            "/fileserver/*",
            FileServerSpringMvcConfig.class,SwaggerConfig.class);

        WebConfig.registerRequestContextListener(servletContext);
        WebConfig.registerSingleSignOutHttpSessionListener(servletContext);
        //WebConfig.registerResponseCorsFilter(servletContext);
        WebConfig.registerCharacterEncodingFilter(servletContext, servletUrlPatterns);
        WebConfig.registerHttpPutFormContentFilter(servletContext, servletUrlPatterns);
        WebConfig.registerHiddenHttpMethodFilter(servletContext, servletUrlPatterns);
        WebConfig.registerRequestThreadLocalFilter(servletContext);
        WebConfig.registerSpringSecurityFilter(servletContext, servletUrlPatterns);

        //dubbo hessian协议使用
        ServletRegistration.Dynamic dubbo = servletContext.addServlet("dubbo", DispatcherServlet.class);
        dubbo.addMapping("/*");


        Properties properties = PropertiesReader.getClassPathProperties(
            WebInitializer.class, "/system.properties");

        String jdbcUrl = properties.getProperty("jdbc.url");

        if(jdbcUrl!=null && jdbcUrl.startsWith("jdbc:h2")){
            WebConfig.initializeH2Console(servletContext);
        }
    }

 }
