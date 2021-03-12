FROM adoptopenjdk/maven-openjdk11:latest
COPY target/quarkus-app /opt/app
WORKDIR /opt/app
CMD ["java", "-jar", "quarkus-run.jar"]
EXPOSE 8080
