# Multi-stage build for PizzaMaestro

# Stage 1: Build frontend
FROM node:25-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci --legacy-peer-deps
COPY frontend/ ./
RUN npm run build

# Stage 2: Build backend
FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /app
COPY pom.xml ./
COPY src ./src
# Kopiuj zbudowany frontend do resources (frontend już zbudowany w stage 1)
COPY --from=frontend-build /app/frontend/build ./src/main/resources/static
# Pomiń budowanie frontendu w Maven - frontend już jest w static
RUN mvn clean package -DskipTests -Dskip.frontend=true

# Stage 3: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Ustaw timezone
RUN apk add --no-cache tzdata
ENV TZ=Europe/Warsaw

# Kopiuj JAR
COPY --from=backend-build /app/target/*.jar app.jar

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Port (Railway używa zmiennej PORT)
EXPOSE 8080
ENV PORT=8080

# Uruchom aplikację
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
