FROM adoptopenjdk/openjdk11:ubi-minimal-jre
COPY target/quarkus-app /opt/app
WORKDIR /opt/app
CMD ["java", "-jar", "quarkus-run.jar"]
EXPOSE 8080
