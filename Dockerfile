# Step 1: Build with Maven inside Docker
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Ensure Java source files do not contain UTF-8 BOM which breaks javac in some environments
RUN find src -type f -name "*.java" -print0 | xargs -0 -n1 sed -i '1s/^\xEF\xBB\xBF//' || true
RUN mvn clean package -DskipTests

# Step 2: Run the Spring Boot app
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT [\"java\", \"-jar\", \"app.jar\"]
