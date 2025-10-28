FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /src

COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package

FROM amazoncorretto:21-alpine-jdk
WORKDIR /opt/app

COPY --from=builder /src/target/*-SNAPSHOT.jar /opt/app/app.jar

EXPOSE 8081
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]
