# syntax=docker/dockerfile:1

# ---------- Tahap build ----------
# Image Maven resmi dipakai supaya Maven sudah tersedia di dalam container.
# Maven Wrapper (./mvnw) sengaja tidak dipakai: skrip itu mengunduh distribusi
# Maven lalu mengekstraknya, dan image JRE/JDK tidak menyediakan `unzip`
# sehingga ekstraksi arsip .zip gagal ("tar: Cannot open: Function not implemented").
FROM maven:3.10-eclipse-temurin-25 AS build
WORKDIR /app

# POM disalin lebih dulu agar cache layer dependency bisa dipakai ulang
COPY pom.xml ./
RUN mvn -B -DskipTests dependency:go-offline

COPY src/ src/

# Profil production menjalankan vaadin:build-frontend, sehingga bundle frontend
# ikut terpaket dan aplikasi tidak berjalan dalam mode development.
RUN mvn -B -Pproduction -DskipTests package

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
