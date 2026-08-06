# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -q dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -Dmaven.test.skip=true package

FROM eclipse-temurin:17-jre
WORKDIR /app

RUN groupadd -r spring && useradd -r -g spring spring
COPY --from=build --chown=spring:spring /app/target/*.jar app.jar
USER spring:spring

EXPOSE 8088
ENTRYPOINT ["java", "-jar", "app.jar"]
