FROM ubuntu:latest
LABEL authors="yanghyojun"

ENTRYPOINT ["top", "-b"]

FROM gradle:9.3.1-jdk17 AS builder
WORKDIR /app

COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew

COPY src src
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /app/build/libs /tmp/libs
RUN set -eux; \
    JAR_PATH="$(find /tmp/libs -maxdepth 1 -name '*.jar' ! -name '*plain*.jar' | head -n 1)"; \
    cp "$JAR_PATH" /app/app.jar; \
    rm -rf /tmp/libs

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]