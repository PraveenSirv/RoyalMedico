# Use OpenJDK base image
FROM openjdk:17-jdk-slim

# Set working directory inside container
WORKDIR /app

# Copy the project’s jar (we’ll name it target/app.jar)
COPY target/*.jar app.jar

# Expose port (change based on service config)
EXPOSE 8761

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]