FROM registry.cn-hangzhou.aliyuncs.com/dockerhub_mirror/java:21-anolis

WORKDIR /pig-media-biz

ARG JAR_FILE=target/pig-media-biz-exec.jar

COPY ${JAR_FILE} app.jar

EXPOSE 4050

ENV TZ=Asia/Shanghai JAVA_OPTS="-Xms128m -Xmx256m -Djava.security.egd=file:/dev/./urandom"

CMD sleep 60; java $JAVA_OPTS -jar app.jar
