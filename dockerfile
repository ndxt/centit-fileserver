FROM 172.29.0.13:8082/tomcat-centit:v1
MAINTAINER hzf "hzf@centit.com"
ADD ./fileserver-web/target/*.war /usr/local/tomcat/webapps/fileserver.war
EXPOSE 8080
CMD /usr/local/tomcat/bin/startup.sh && tail -f /usr/local/tomcat/logs/catalina.out