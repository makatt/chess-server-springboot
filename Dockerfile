# syntax=docker/dockerfile:1.7
# =============================================================================
#  chess-server-springboot — multi-stage образ для chess-сервера (Spring Boot 3 / Java 17)
#  Stage 1: сборка Maven с кэшем зависимостей в отдельном слое
#  Stage 2: только JRE + jar, non-root пользователь, без компилятора внутри
# =============================================================================

# ------------------------------- stage 1: build ------------------------------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src

# Слой с зависимостями: инвалидируется только при изменении pom.xml
COPY pom.xml ./
RUN mvn -B -ntp -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -ntp -DskipTests package \
 && cp target/*.jar /src/app.jar

# ------------------------------ stage 2: runtime ----------------------------
FROM eclipse-temurin:17-jre-jammy AS runtime

# curl — для HEALTHCHECK; tini — PID 1, корректно пробрасывает SIGTERM (graceful shutdown)
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl tini \
 && rm -rf /var/lib/apt/lists/* \
 && groupadd --gid 10001 app \
 && useradd --uid 10001 --gid app --home-dir /app --shell /usr/sbin/nologin --no-create-home app

WORKDIR /app
COPY --from=build --chown=10001:10001 /src/app.jar /app/app.jar

# Контейнер не работает от root: uid/gid 10001
USER 10001:10001
EXPOSE 8080

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError" \
    SPRING_PROFILES_ACTIVE=""

HEALTHCHECK --interval=15s --timeout=5s --start-period=45s --retries=5 \
  CMD curl -fsS http://127.0.0.1:8080/actuator/health/readiness || exit 1

ENTRYPOINT ["/usr/bin/tini", "--"]
CMD ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
