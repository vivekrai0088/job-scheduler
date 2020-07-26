FROM openjdk:8-jdk-alpine

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

ARG JAR_FILE=server/build/libs/server-1.0-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","/app.jar"]

