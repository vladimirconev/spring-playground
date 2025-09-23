FROM  openjdk:25

ARG JAR_FILE=target/*.jar

WORKDIR /opt/app

COPY ${JAR_FILE} app.jar

ENV JDK_JAVA_OPTIONS="-Xmx512m -XX:+UseG1GC"
ENTRYPOINT ["java","-jar","app.jar"]