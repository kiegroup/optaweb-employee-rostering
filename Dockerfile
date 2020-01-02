# Copyright 2019 Red Hat, Inc. and/or its affiliates.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# This is a multi-staged Dockerfile that uses Maven builder image to build the whole project with Maven.
# In the second phase, the standalone Spring Boot executable JAR is placed into an OpenJDK JRE image.
# Example usage:
# docker build -t optaweb/employee-rostering .
# docker run -p 8080:8080 --rm -it -e SPRING_PROFILES_ACTIVE=dev optaweb/employee-rostering

FROM adoptopenjdk/maven-openjdk8:latest as builder
WORKDIR /usr/src/optaweb
COPY . .
RUN mvn clean install -DskipTests

FROM adoptopenjdk/openjdk8:ubi-minimal-jre
RUN mkdir /opt/app
COPY --from=builder /usr/src/optaweb/employee-rostering-standalone/target/*-exec.jar /opt/app/optaweb-employee-rostering.jar
CMD ["java", "-jar", "/opt/app/optaweb-employee-rostering.jar"]
EXPOSE 8080
