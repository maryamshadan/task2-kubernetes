Project task2-rest-api

docker build -t task2-rest-api:local .
This project targets Java 17.

Local setup
1. Install Temurin JDK 17 (or another JDK 17 distribution) and ensure `java` and `javac` are on your PATH.
   - Windows (winget): `winget install -e --id EclipseAdoptium.Temurin.17.JDK`

2. Install Maven and ensure `mvn` is on your PATH.
   - Windows (winget): `winget install -e --id Apache.Maven`

Build locally
```powershell
mvn -DskipTests package
```

Docker
- The repository Dockerfiles are updated to use Java 17. Build the image with:
```powershell
docker build -t task2-rest-api:local .
```
