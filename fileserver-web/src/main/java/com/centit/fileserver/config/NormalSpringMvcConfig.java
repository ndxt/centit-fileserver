package com.centit.fileserver.config;

import com.centit.framework.config.WebBeanConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by zou_wy on 2017/3/29.
 */
@Configuration
@Import(WebBeanConfig.class)
@ComponentScan(basePackages = {"com.centit.fileserver.controller"},
        includeFilters = {@ComponentScan.Filter(value= org.springframework.stereotype.Controller.class)},
        useDefaultFilters = false)
public class NormalSpringMvcConfig extends WebMvcConfigurerAdapter {

}
