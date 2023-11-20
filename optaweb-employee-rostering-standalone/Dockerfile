FROM docker.io/adoptopenjdk/openjdk11:ubi-minimal
COPY target/quarkus-app /opt/app
WORKDIR /opt/app
CMD ["java", "-jar", "quarkus-run.jar"]
EXPOSE 8080
