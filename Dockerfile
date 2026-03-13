FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy Maven wrapper
COPY mvnw .
COPY .mvn .mvn

# Copy project files
COPY pom.xml .
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/code-grading-system-0.0.1-SNAPSHOT.jar"]