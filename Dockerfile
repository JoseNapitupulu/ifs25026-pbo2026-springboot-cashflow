# syntax=docker/dockerfile:1

# ---------- Tahap build ----------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Wrapper dan POM disalin lebih dulu agar cache layer dependency bisa dipakai ulang
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw \
    && ./mvnw -B -DskipTests dependency:go-offline

COPY src/ src/

# Profil production menjalankan vaadin:build-frontend, sehingga bundle frontend
# ikut terpaket dan aplikasi tidak berjalan dalam mode development.
RUN ./mvnw -B -Pproduction -DskipTests package

# ---------- Tahap runtime ----------
FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app

RUN useradd --system --uid 1001 --create-home appuser
COPY --from=build /app/target/*.jar app.jar
USER appuser

# application.yaml membaca ${APP_PORT}; nilai ini bisa ditimpa lewat dashboard Render
ENV APP_PORT=10000
EXPOSE 10000

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
