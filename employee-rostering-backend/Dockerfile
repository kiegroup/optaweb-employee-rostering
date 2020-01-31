FROM adoptopenjdk/openjdk8:ubi-minimal-jre
COPY target/*-exec.jar /opt/app/optaweb-employee-rostering.jar
WORKDIR /opt/app
CMD ["java", "-jar", "optaweb-employee-rostering.jar"]
EXPOSE 8080
