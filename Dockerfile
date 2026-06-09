# Step 1: Build stage using Maven and JDK 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and download dependencies to utilize Docker layer caching
COPY wallet/pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and compile the production JAR artifact
COPY wallet/src ./src
RUN mvn clean package -DskipTests

# Step 2: Ultra-lightweight runtime stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the compiled JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the web server port
EXPOSE 8080

# Execute the application
ENTRYPOINT ["java", "-jar", "app.jar"]