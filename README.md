<div align="center">

```
███████╗███████╗███╗   ██╗████████╗██╗███╗   ██╗███████╗██╗
██╔════╝██╔════╝████╗  ██║╚══██╔══╝██║████╗  ██║██╔════╝██║
███████╗█████╗  ██╔██╗ ██║   ██║   ██║██╔██╗ ██║█████╗  ██║
╚════██║██╔══╝  ██║╚██╗██║   ██║   ██║██║╚██╗██║██╔══╝  ██║
███████║███████╗██║ ╚████║   ██║   ██║██║ ╚████║███████╗███████╗
╚══════╝╚══════╝╚═╝  ╚═══╝   ╚═╝   ╚═╝╚═╝  ╚═══╝╚══════╝╚══════╝
```

### 🛰️ CRYPTO SENTINEL — *Delysz Edition*
**Vigilancia de activos digitales. Tiempo real. Sin piedad.**

<br>

![Java](https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Telegram](https://img.shields.io/badge/Telegram_Bot-26A5E4?style=for-the-badge&logo=telegram&logoColor=white)
![CoinGecko](https://img.shields.io/badge/CoinGecko_API-8DC63F?style=for-the-badge&logo=gecko&logoColor=white)

![Status](https://img.shields.io/badge/estado-operativo-00ffe7?style=flat-square&labelColor=0d1117)
![License](https://img.shields.io/badge/licencia-MIT-9d50bb?style=flat-square&labelColor=0d1117)
![Render](https://img.shields.io/badge/desplegado_en-Render-46E3B7?style=flat-square&labelColor=0d1117)

</div>

---

## ¿Qué es esto?

**Crypto Sentinel** no es un dashboard más. Es un sistema de vigilancia persistente: observa el mercado mientras tú duermes, detecta caídas antes de que las veas y te avisa directamente al móvil. Construido con Java, desplegado en la nube, protegido con Spring Security y conectado a Telegram.

Esta edición está personalizada con la marca **Delysz** — estética neón, interfaz oscura profesional e integración nativa con `@Delysz_bot`.

---

## ✦ Características

| Módulo | Descripción |
|--------|-------------|
| 📡 **Vigilancia continua** | Polling automático a CoinGecko cada N minutos para todos los activos registrados |
| 🔐 **Doble capa de acceso** | Vista pública de solo lectura + panel privado de gestión protegido con Spring Security |
| 🤖 **Alertas Telegram** | Notificación automática vía bot cuando un activo cae por encima del umbral configurado |
| 🧠 **Fear & Greed Index** | Indicador de sentimiento de mercado en tiempo real en la cabecera del dashboard |
| 📊 **Sparklines 15m** | Gráficos de tendencia por activo generados con Chart.js sin dependencias externas |
| 🗄️ **Persistencia** | Base de datos H2 en modo archivo — los datos sobreviven a reinicios del servidor |
| 🐳 **Docker ready** | Imagen contenedorizada y lista para desplegar en Render o cualquier cloud provider |

---

## 🛠️ Stack Tecnológico

```
┌─────────────────────────────────────────────────────────┐
│                    CRYPTO SENTINEL                      │
├──────────────────────┬──────────────────────────────────┤
│  BACKEND             │  FRONTEND                        │
│  ─────────────────   │  ──────────────────────────────  │
│  Java 21             │  Thymeleaf (server-side SSR)     │
│  Spring Boot 3.4.3   │  Bootstrap 5.3 (Dark Mode)       │
│  Spring Security     │  Chart.js (sparklines)           │
│  Spring Data JPA     │  Bootstrap Icons                 │
│  H2 Database         │  CSS Custom Properties + Blur    │
├──────────────────────┴──────────────────────────────────┤
│  INTEGRACIONES                                          │
│  ─────────────────────────────────────────────────────  │
│  CoinGecko API  →  precios + historial                  │
│  Telegram Bot API  →  alertas push en tiempo real       │
│  Alternative.me  →  Fear & Greed Index                  │
├──────────────────────────────────────────────────────────┤
│  INFRAESTRUCTURA                                        │
│  ─────────────────────────────────────────────────────  │
│  Docker  →  contenedorización                           │
│  Render  →  despliegue continuo                         │
└─────────────────────────────────────────────────────────┘
```

---

## ⚙️ Variables de Entorno

El sistema no guarda credenciales en el código. Todo se inyecta vía variables de entorno:

```env
# ── Telegram ────────────────────────────────────
TELEGRAM_BOT_TOKEN=xxxx:xxxxxxxxxxxxxxxxxxxxxxxxxx
TELEGRAM_CHAT_ID=-100xxxxxxxxxx

# ── Spring Security ──────────────────────────────
SPRING_SECURITY_USER_NAME=delysz
SPRING_SECURITY_USER_PASSWORD=tu_password_privado
```

| Variable | Descripción | Dónde obtenerla |
|:---|:---|:---|
| `TELEGRAM_BOT_TOKEN` | Token del bot de alertas | [@BotFather](https://t.me/BotFather) en Telegram |
| `TELEGRAM_CHAT_ID` | Canal o chat destino de las alertas | [@userinfobot](https://t.me/userinfobot) o logs del bot |
| `SPRING_SECURITY_USER_NAME` | Usuario del panel de administración | Tú lo defines |
| `SPRING_SECURITY_USER_PASSWORD` | Contraseña del panel de administración | Tú la defines |

---

## 🚀 Instalación Local

**Requisitos previos:** Java 21+, Maven, Git.

```bash
# 1. Clona el repositorio
git clone https://github.com/delysz/crypto-sentinel.git
cd crypto-sentinel

# 2. Define las variables de entorno en tu terminal (o en IntelliJ → Run Configurations)
export TELEGRAM_BOT_TOKEN="tu_token"
export TELEGRAM_CHAT_ID="tu_id"
export SPRING_SECURITY_USER_NAME="delysz"
export SPRING_SECURITY_USER_PASSWORD="tu_password"

# 3. Arranca el servidor
./mvnw spring-boot:run

# 4. Abre el dashboard
open http://localhost:8080
```

> 💡 **Consejo:** En IntelliJ, configura las variables en `Run > Edit Configurations > Environment Variables` para no tener que exportarlas manualmente cada vez.

---

## 🐳 Docker

El proyecto incluye un `Dockerfile` listo para producción. Sin configuración adicional.

```bash
# Construir la imagen
docker build -t crypto-sentinel .

# Ejecutar el contenedor
docker run -d \
  --name sentinel \
  -p 8080:8080 \
  -e TELEGRAM_BOT_TOKEN="tu_token" \
  -e TELEGRAM_CHAT_ID="tu_id" \
  -e SPRING_SECURITY_USER_NAME="delysz" \
  -e SPRING_SECURITY_USER_PASSWORD="tu_password" \
  -v sentinel-data:/app/data \
  crypto-sentinel
```

```bash
# Ver logs en tiempo real
docker logs -f sentinel

# Parar el centinela
docker stop sentinel
```

---

## 🔐 Arquitectura de Seguridad

```
Usuario anónimo                    Usuario autenticado (admin)
─────────────────                  ──────────────────────────
✅ Ver dashboard                   ✅ Ver dashboard
✅ Ver alertas                     ✅ Ver alertas
✅ Fear & Greed Index              ✅ Fear & Greed Index
❌ Añadir activos                  ✅ Añadir activos al radar
❌ Eliminar activos                ✅ Eliminar activos del radar
                                   ✅ Acceso al panel /admin
```

El acceso al panel de gestión está protegido por **Spring Security** con autenticación basada en variables de entorno. Ninguna credencial vive en el código fuente.

---

## 📡 Flujo de Alertas

```
  Scheduler (cron)
       │
       ▼
  CoinGecko API ──► ¿precio actual vs precio anterior?
                          │
              ┌───────────┴───────────┐
           CAÍDA < umbral          CAÍDA ≥ umbral
              │                       │
          Sin acción          Registrar en BBDD
                                       │
                               Telegram Bot API
                                       │
                              📱 Alerta en tu móvil
```

---

## 🤝 Únete al Centinela

¿Quieres recibir las alertas de este sistema directamente en tu Telegram sin montar nada?

<div align="center">

[![Seguir @Delysz_bot](https://img.shields.io/badge/Telegram-Seguir%20%40Delysz__bot-26A5E4?style=for-the-badge&logo=telegram&logoColor=white)](https://t.me/Delysz_bot)

**Alertas automáticas. Gratuito. Sin configuración.**

</div>

---

<div align="center">

*Desarrollado con precisión y pasión por el orden por* **delysz** *·* `2026`

```
◆ — el mercado no duerme, el sentinel tampoco — ◆
```

</div>
