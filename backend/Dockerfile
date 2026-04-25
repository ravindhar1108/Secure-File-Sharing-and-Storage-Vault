# Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy everything (including mvnw + .mvn)
COPY . .

# Give permission to mvnw
RUN chmod +x mvnw

# Build using Maven Wrapper
RUN ./mvnw clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]