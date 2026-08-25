# syntax=docker/dockerfile:1

FROM maven:3.9.12-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline

COPY src ./src
RUN mvn -B -ntp clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S linkops && adduser -S linkops -G linkops
COPY --from=build /workspace/target/linkops-api-*.jar /app/linkops-api.jar

USER linkops
EXPOSE 8081

HEALTHCHECK --interval=15s --timeout=5s --start-period=40s --retries=5 \
  CMD wget -q -O /dev/null http://localhost:8081/api/v1/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/linkops-api.jar"]
