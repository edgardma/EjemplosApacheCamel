
# docker/Dockerfile
FROM eclipse-temurin:17-jre
WORKDIR /app
ARG JAR=build/libs/camel-pipeline-1.0.0.jar
COPY ${JAR} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
