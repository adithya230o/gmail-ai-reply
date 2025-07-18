# Step 1: Build the JAR using Maven
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Step 2: Use OpenJDK to run the JAR
FROM openjdk:17
WORKDIR /app
COPY --from=builder /app/target/gmail-ai-reply.jar gmail-ai-reply.jar
ENTRYPOINT ["java", "-jar", "/app/gmail-ai-reply.jar"]
