# Use a lightweight OpenJDK base image
FROM openjdk:17-jdk-slim

# Set working directory inside container
WORKDIR /app

# Copy jar to container
COPY target/gmail-ai-reply.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
