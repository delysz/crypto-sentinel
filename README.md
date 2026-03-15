# 🚀 Crypto Sentinel Dashboard

Crypto Sentinel es una aplicación web desarrollada en **Spring Boot** que monitoriza el mercado de criptomonedas en tiempo real. Obtiene precios actualizados, analiza el sentimiento del mercado general y envía alertas automáticas a tu móvil mediante Telegram cuando un activo cae por debajo de un umbral específico.

## ✨ Características Principales

* **Monitorización en Tiempo Real:** Integración con la API de CoinGecko para obtener precios y variaciones actualizadas.
* **Alertas Inteligentes (Telegram):** Notificaciones push directas a tu móvil cuando se detectan caídas críticas en el mercado.
* **Inteligencia de Mercado:** Integración con la API de Alternative.me para mostrar el Índice de Miedo y Codicia (Fear & Greed Index).
* **Gráficos Dinámicos:** Sparklines (minigráficas) generadas con Chart.js para visualizar la tendencia de precios de los últimos 15 minutos.
* **Persistencia de Datos:** Base de datos H2 local para mantener el historial de alertas y el registro de las monedas bajo vigilancia.
* **Interfaz Moderna:** Dashboard estilizado con Bootstrap 5, diseño responsivo y Dark Mode nativo.

## 🛠️ Tecnologías Utilizadas

* **Backend:** Java 21, Spring Boot, Spring Data JPA, Hibernate.
* **Frontend:** HTML5, Thymeleaf, Bootstrap 5.3, Chart.js.
* **Base de Datos:** H2 Database.
* **Integraciones:** Telegram Bot API, CoinGecko API, Fear & Greed API.
* **Despliegue:** Docker.

## 🚀 Cómo ejecutarlo localmente

1. Clona este repositorio:
   ```bash
   git clone [https://github.com/delysz/crypto-sentinel.git](https://github.com/delysz/crypto-sentinel.git)
   ```

2. Configura las variables de entorno en tu sistema o IDE para proteger tus credenciales:
    * `TELEGRAM_BOT_TOKEN`: El token de tu bot provisto por BotFather.
    * `TELEGRAM_CHAT_ID`: Tu ID de chat personal.

3. Compila el proyecto con Maven:
   ```bash
   ./mvnw clean package -DskipTests
   ```

4. Ejecuta la aplicación:
   ```bash
   ./mvnw spring-boot:run
   ```

5. Abre tu navegador y accede a http://localhost:8080

## 🐳 Despliegue con Docker

El proyecto incluye un `Dockerfile` listo para producción. Puedes construir la imagen y levantar el contenedor inyectando las variables de entorno de forma segura:

```bash
# 1. Construir la imagen
docker build -t crypto-sentinel .

# 2. Ejecutar el contenedor con volumen persistente
docker run -d \
  -p 8080:8080 \
  -e TELEGRAM_BOT_TOKEN="tu_token_aqui" \
  -e TELEGRAM_CHAT_ID="tu_chat_id_aqui" \
  -v sentinel-data:/app/data \
  --name mi-centinela \
  crypto-sentinel
```

## 📜 Licencia
Este proyecto es de código abierto y está disponible bajo la Licencia MIT.
