#!/bin/sh
#--------------------------------------------
#鍙傛暟璇存槑:sh build-deploy.sh ${DEPLOYMENT} ${MODULE}
#鏈緥鎵ц:sh build-deploy.sh fileserver fileserver-web
#--------------------------------------------
DEPLOYMENT=$1
MOUDLE=$2
TIME=$(date +%Y%m%d%H%M)
GIT_REVISION=$(git log -1 --pretty=format:"%h")
IMAGE_NAME=172.29.0.13:8082/${DEPLOYMENT}:${TIME}_${GIT_REVISION}
########缂栬瘧########
mvn -U -pl ${MOUDLE} -am clean package -DskipTests=true
########鍐欏叆dockerfile鏂囦欢########
cat >./${MOUDLE}/Dockerfile <<EOF
FROM tomcat
MAINTAINER hzf "hzf@centit.com"
ADD target/*.war /usr/local/tomcat/webapps/${DEPLOYMENT}.war
EXPOSE 8080
CMD /usr/local/tomcat/bin/startup.sh && tail -f /usr/local/tomcat/logs/catalina.out
EOF
########鏋勫缓闀滃儚########
cd ./${MOUDLE}/
docker build -t ${IMAGE_NAME} .
########涓婁紶nexus########
docker login -u developer -p centit 172.29.0.13:8082
docker push ${IMAGE_NAME}
########鍒犻櫎瀹瑰櫒########
docker rm -f ${DEPLOYMENT} |true
########鍒犻櫎闀滃儚########
docker image rm ${IMAGE_NAME} |true
docker images|grep none|awk '{print $3}'|xargs docker rmi |true
########杩炴帴nexus绉佹湇########
docker login -u developer -p centit 172.29.0.13:8082
########杩愯########
docker run -d -p 14000:8080 --name ${DEPLOYMENT} ${IMAGE_NAME}