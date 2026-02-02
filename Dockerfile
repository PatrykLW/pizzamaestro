# Multi-stage build for PizzaMaestro

# Stage 1: Build frontend (Node 20 LTS - stable)
FROM node:20-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm install --legacy-peer-deps
COPY frontend/ ./
RUN npm run build

# Stage 2: Build backend (Java 21 LTS - stable)
FROM maven:3-eclipse-temurin-25 AS backend-build
WORKDIR /app
COPY pom.xml ./
COPY src ./src
# Kopiuj zbudowany frontend do resources (frontend już zbudowany w stage 1)
COPY --from=frontend-build /app/frontend/build ./src/main/resources/static
# Pomiń budowanie frontendu w Maven - frontend już jest w static
RUN mvn clean package -DskipTests -Dskip.frontend=true

# Stage 3: Runtime (Java 21 LTS - stable)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install wget for healthcheck and set timezone
RUN apk add --no-cache tzdata wget && \
    addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

ENV TZ=Europe/Warsaw

# Copy JAR with specific name pattern
COPY --from=backend-build /app/target/pizzamaestro-*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appgroup /app

# Switch to non-root user for security
USER appuser

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Port (Railway uses PORT variable)
EXPOSE 8080
ENV PORT=8080

# Run with JVM container support and memory tuning
ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Dserver.port=${PORT:-8080} -jar app.jar"]
