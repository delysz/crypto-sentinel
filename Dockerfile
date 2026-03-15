# ==========================================
# ETAPA 1: CONSTRUCCIÓN (El obrero)
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos los archivos de configuración y el código fuente
COPY pom.xml .
COPY src ./src

# Le decimos a Docker que compile el proyecto (igual que hacías en tu terminal)
RUN mvn clean package -Dmaven.test.skip=true

# ==========================================
# ETAPA 2: EJECUCIÓN (El servidor final)
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiamos el .jar que acaba de fabricar la Etapa 1 a esta nueva máquina limpia
COPY --from=build /app/target/demo-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]