# Etapa 1: Construcción (Build)
FROM maven:3.9.8-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copiar el pom.xml y descargar dependencias (esto optimiza la caché de Docker)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución (Runtime)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]