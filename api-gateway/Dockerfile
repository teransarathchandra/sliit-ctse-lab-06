# syntax=docker/dockerfile:1
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY inventory-service/pom.xml inventory-service/pom.xml
COPY order-service/pom.xml order-service/pom.xml
COPY billing-service/pom.xml billing-service/pom.xml

RUN mvn -q -DskipTests -pl :api-gateway -am dependency:go-offline

COPY api-gateway/src api-gateway/src
RUN mvn -q -DskipTests -pl :api-gateway -am package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /workspace/api-gateway/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
