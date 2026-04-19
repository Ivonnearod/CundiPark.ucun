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
# Instalamos el cliente de PostgreSQL para que los comandos de backup (pg_dump) funcionen
RUN apk add --no-cache postgresql-client
WORKDIR /app
# Creamos las carpetas necesarias para que la app no falle al intentar escribir
RUN mkdir logs backups uploads
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]