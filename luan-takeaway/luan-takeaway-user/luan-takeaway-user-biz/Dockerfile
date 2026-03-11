FROM registry.cn-hangzhou.aliyuncs.com/dockerhub_mirror/java:21-anolis

WORKDIR /pig-takeaway-user-biz

ARG JAR_FILE=target/pig-takeaway-user-biz-exec.jar

COPY ${JAR_FILE} app.jar

EXPOSE 4501

ENV TZ=Asia/Shanghai JAVA_OPTS="-Xms256m -Xmx512m -Djava.security.egd=file:/dev/./urandom"

CMD sleep 60; java $JAVA_OPTS -jar app.jar