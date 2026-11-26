# Stage 1: Build the executable Spring Boot JAR
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests clean package

# Stage 2: Lightweight Java 21 JRE runtime
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring spring

COPY --from=build /app/target/*.jar app.jar
RUN chown spring:spring app.jar
USER spring:spring

ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
