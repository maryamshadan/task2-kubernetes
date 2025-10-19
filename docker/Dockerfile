# Stage 1: Build JAR
FROM maven:3.9.3-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/task2-rest-api-1.0.0.jar app.jar

# Expose port
EXPOSE 8080

# Run the JAR
ENTRYPOINT ["java","-jar","app.jar"]
# Stage 1: Build JAR
FROM maven:3.9.3-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/task2-rest-api-1.0.0.jar app.jar

# Expose port
EXPOSE 8080

# Run the JAR
ENTRYPOINT ["java","-jar","app.jar"]



