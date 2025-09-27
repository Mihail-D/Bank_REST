# Multi-stage Dockerfile for Bank_REST

# Build stage
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml ./
# Кешируем зависимости
RUN --mount=type=cache,target=/root/.m2 mvn -q -e -B -DskipTests dependency:go-offline
# Копируем исходники и собираем
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -e -B -DskipTests package

# Runtime stage
FROM eclipse-temurin:17-jre
ENV JAVA_OPTS=""
WORKDIR /app
COPY --from=build /workspace/target/*-SNAPSHOT.jar /app/app.jar
EXPOSE 8081
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

