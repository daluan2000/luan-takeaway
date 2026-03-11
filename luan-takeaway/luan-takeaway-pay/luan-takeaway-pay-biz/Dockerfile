FROM registry.cn-hangzhou.aliyuncs.com/dockerhub_mirror/java:21-anolis

WORKDIR /luan-takeaway-pay-biz

ARG JAR_FILE=target/luan-takeaway-pay-biz-exec.jar

COPY ${JAR_FILE} app.jar

EXPOSE 4504

ENV TZ=Asia/Shanghai JAVA_OPTS="-Xms128m -Xmx256m -Djava.security.egd=file:/dev/./urandom"

CMD sleep 60; java $JAVA_OPTS -jar app.jar
