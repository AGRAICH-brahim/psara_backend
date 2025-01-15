# Étape 1 : Construire l'application
FROM maven:3.8.5-openjdk-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : Image finale avec l'application Spring Boot
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/target/discovery-*.jar app.jar

# Exposer le port Eureka
EXPOSE 8761

# Démarrer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
