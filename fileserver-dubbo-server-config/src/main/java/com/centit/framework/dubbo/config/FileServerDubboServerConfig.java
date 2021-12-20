package com.centit.framework.dubbo.config;

import org.apache.dubbo.config.ProtocolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.Query;
import java.util.Iterator;
import java.util.Set;

@Configuration
@ImportResource({"classpath:dubbo-fileserver-server.xml"})
public class FileServerDubboServerConfig  {
    Logger logger = LoggerFactory.getLogger(FileServerDubboServerConfig.class);

    @Value("${centit.dubbo.hessianprotocol.name:hessian}")
    private String hessianProtocolName;
    @Value("${centit.dubbo.hessianprotocol.server:servlet}")
    private String hessianProtocolServer;
    //该端口必须和tomcat端口一致   默认8080
 /*   @Value("${centit.dubbo.hessianprotocol.port}")
    private Integer hessianProtocolPort;*/
    @Value("${centit.dubbo.hessianprotocol.contextpath:}")
    private String contextpath;

    @Bean
    public ProtocolConfig hessianProtocolConfig() {
        ProtocolConfig protocolConfig = new ProtocolConfig();
        protocolConfig.setName(hessianProtocolName);
        protocolConfig.setServer(hessianProtocolServer);
        protocolConfig.setPort(getHttpPort());
        protocolConfig.setContextpath(contextpath);
        return protocolConfig;
    }

    public int getHttpPort() {
        try {
            MBeanServer server;
            if (MBeanServerFactory.findMBeanServer(null).size() > 0) {
                server = MBeanServerFactory.findMBeanServer(null).get(0);
            } else {
                logger.error("Obtaining the Hessian protocol port is abnormal,messageInfo：no MBeanServer!");
                return 8080;
            }
            Set names = server.queryNames(new ObjectName("Catalina:type=Connector,*"),
                Query.match(Query.attr("protocol"), Query.value("HTTP/1.1")));
            Iterator iterator = names.iterator();
            if (iterator.hasNext()) {
                ObjectName name = (ObjectName) iterator.next();
                int port = Integer.parseInt(server.getAttribute(name, "port").toString());
                logger.info("The hessian protocol port is："+port);
                return port;
            }
        } catch (Exception e) {
            logger.error("Obtaining the Hessian protocol port is abnormal，messageInfo："+e.getMessage());
        }
        return -1;
    }

}
