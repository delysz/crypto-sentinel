# Usamos una imagen ligera de Java 21
FROM eclipse-temurin:21-jdk-alpine

# Creamos la carpeta de trabajo
WORKDIR /app

# Copiamos tu archivo exacto y lo renombramos a app.jar dentro del contenedor
COPY target/demo-0.0.1-SNAPSHOT.jar app.jar

# Exponemos el puerto
EXPOSE 8080

# El comando de arranque
ENTRYPOINT ["java", "-jar", "app.jar"]