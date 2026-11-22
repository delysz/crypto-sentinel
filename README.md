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
| 📡 **Vigilancia continua** | Polling automático cada 5 minutos a CoinGecko (cripto) y Yahoo Finance (ETFs y acciones) |
| 🏦 **Multi-activo** | Criptomonedas, ETFs y acciones en el mismo radar. Los ETFs/acciones de `sentinel.etf.symbols` y `sentinel.stock.symbols` se cargan solos al arrancar |
| 💼 **Mi Cartera** | Registra cuánto tienes de cada activo y a qué precio medio lo compraste. Valor real (cantidad × precio), reparto por tipo y ganancia/pérdida por activo y total |
| 📈 **Histórico de rendimiento** | Snapshot diario automático (23:55) del valor de la cartera, con gráfico de evolución en el dashboard |
| ⚖️ **Evaluación de riesgo** | Puntuación 0–100 (bajo / medio / alto) según la exposición a cripto y la concentración de la cartera, con recomendación |
| 🎯 **Alertas de precio** | Define un precio objetivo ("sube por encima de" / "cae por debajo de") por activo; al cumplirse te avisa por Telegram y la alerta se desactiva |
| 🤖 **Alertas de caída** | Notificación automática vía bot cuando un activo cae más del umbral configurado entre dos lecturas |
| 🧠 **Fear & Greed Index** | Indicador de sentimiento de mercado en tiempo real en la cabecera del dashboard |
| 📊 **Gráficos** | Sparklines por activo, donut de distribución y línea de rendimiento con Chart.js |
| 📤 **Exportar CSV** | Descarga de todas tus posiciones con valor, invertido y P&L |
| 🔐 **Doble capa de acceso** | Vista pública de solo lectura + panel privado protegido con Spring Security y CSRF |
| 🗄️ **Persistencia** | Base de datos H2 en modo archivo — los datos sobreviven a reinicios del servidor |
| 🐳 **Docker ready** | Imagen contenedorizada y lista para desplegar en Render o cualquier cloud provider |

---

## 💼 Mi Cartera

Tras iniciar sesión, el dashboard muestra la sección **Mi Cartera**:

- **KPIs:** valor total, capital invertido, ganancia/pérdida (en $ y %) y nivel de riesgo (botón *Recalcular*).
- **Distribución:** donut con el peso de cripto, ETFs y acciones.
- **Rendimiento histórico:** evolución del valor total y por tipo (7 / 30 / 90 / 365 días).
- **Posiciones:** tabla ordenable con edición en línea de cantidad y precio medio de compra (acepta `1,5` o `1.5`).
- **Alertas de precio:** formulario para crear alertas y lista de alertas activas.

> ⚠️ Los activos nuevos empiezan con **cantidad 0**: introduce tus cantidades en la tabla de posiciones para que la cartera tenga valor.

---

## 📄 API

Todas las rutas salvo `GET /` requieren sesión iniciada. Las peticiones que modifican datos (`POST`, `PUT`, `DELETE`) requieren además el **token CSRF** (el dashboard lo envía automáticamente).

### Dashboard

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/` | Dashboard: activos vigilados, historial de alertas, Fear & Greed y (con sesión) Mi Cartera |
| `POST` | `/add` | Añade una cripto al radar. Parámetro de formulario `symbol` = ID de CoinGecko (`bitcoin`, `solana`…). Consulta el precio al momento e ignora duplicados |
| `POST` | `/delete/{id}` | Deja de vigilar un activo |

### Cartera — `/api/portfolio`

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/summary` | `{cryptoValue, etfValue, stockValue, totalValue, totalInvested, profitLoss, profitLossPercentage}` |
| `GET` | `/total` | Valor total de la cartera |
| `GET` | `/holdings` | Posiciones: símbolo, tipo, cantidad, precio medio, precio actual, valor, invertido y P&L |
| `PUT` | `/holdings/{symbol}` | Actualiza una posición. Body: `{"quantity": 0.5, "averageBuyPrice": 60000}` (`averageBuyPrice` puede ser `null`). `400` si los datos no son válidos, `404` si el activo no existe |
| `GET` | `/export.csv` | Descarga las posiciones en CSV |
| `GET` | `/performance/history?days=30` | Snapshots diarios de los últimos N días |
| `GET` | `/performance/current` | Último snapshot |
| `POST` | `/performance/snapshot` | Fuerza un snapshot ahora (sustituye al del mismo día) |
| `POST` | `/risk` | Calcula y guarda una evaluación de riesgo (`204` si la cartera está vacía) |
| `GET` | `/risk` | Última evaluación de riesgo (`404` si no hay ninguna) |
| `GET` | `/risk/history` | Histórico de evaluaciones |

### Alertas de precio — `/api/alerts`

| Método | Ruta | Descripción |
|--------|------|-------------|
| `POST` | `/` | Crea una alerta. Body: `{"assetSymbol": "bitcoin", "targetPrice": 150000, "alertType": "above"}` (`above` / `below`). `201` si se crea, `400 {"error": "..."}` si no es válida |
| `GET` | `/` | Alertas activas |
| `GET` | `/all` | Todas las alertas, las más recientes primero |
| `GET` | `/asset/{symbol}` | Alertas activas de un activo |
| `GET` | `/{id}` | Una alerta |
| `DELETE` | `/{id}` | Borra una alerta (`404` si no existe) |

---

## 📦 Modelo de Datos

Base de datos H2; las definiciones están en `src/main/java/com/example/demo/model`.

| Entidad | Campos principales |
|---------|--------------------|
| **CryptoAsset** | `symbol`, `name`, `type` (CRYPTO / ETF / STOCK), `price`, `lastPrice`, `priceHistory` (últimas 15 lecturas), `quantity`, `averageBuyPrice`, `lastUpdated` |
| **AlertLog** | `cryptoName`, `priceAtAlert`, `dropPercentage`, `timestamp` — historial de alertas de caída |
| **PriceAlert** | `assetSymbol`, `assetName`, `targetPrice`, `alertType` (above / below), `active`, `createdDate`, `triggeredDate` |
| **PortfolioPerformance** | `date`, `totalValue`, `cryptoValue`, `etfValue`, `stockValue`, `dailyChange`, `dailyChangePercentage` |
| **RiskAssessment** | `portfolioName`, `riskScore`, `riskLevel`, `recommendation`, `assetDistribution`, `assessmentDate` |

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
│  Spring Security     │  Chart.js (gráficos)             │
│  Spring Data JPA     │  Bootstrap Icons                 │
│  H2 Database         │  CSS Custom Properties + Blur    │
├──────────────────────┴──────────────────────────────────┤
│  INTEGRACIONES                                          │
│  ─────────────────────────────────────────────────────  │
│  CoinGecko API  →  precios cripto                       │
│  Yahoo Finance  →  precios de ETFs y acciones           │
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

**Requisitos previos:** Java 21+ (probado también con JDK 25), Git. Maven no hace falta: el proyecto incluye `./mvnw`.

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
✅ Ver alertas de caída            ✅ Ver alertas de caída
✅ Fear & Greed Index              ✅ Fear & Greed Index
❌ Mi Cartera                      ✅ Mi Cartera (posiciones, P&L, riesgo)
❌ Alertas de precio               ✅ Crear / borrar alertas de precio
❌ Añadir / eliminar activos       ✅ Añadir / eliminar activos del radar
❌ API REST (/api/**)              ✅ API REST (/api/**)
```

El acceso al panel de gestión está protegido por **Spring Security** con autenticación basada en variables de entorno. Ninguna credencial vive en el código fuente.

La **protección CSRF está activa**: los formularios incluyen el token y el JavaScript del dashboard lo envía en una cabecera. Todas las acciones que modifican datos usan `POST`, `PUT` o `DELETE` (nunca un enlace `GET`).

---

## 📡 Tareas Automáticas

| Tarea | Frecuencia | Qué hace |
|-------|-----------|----------|
| `MonitoringService` | cada 5 min | Actualiza el precio de todos los activos y avisa si alguno cae más del umbral |
| `PriceAlertScheduler` | cada 5 min | Comprueba las alertas de precio activas y avisa de las que se cumplen |
| `PortfolioSnapshotScheduler` | diario, 23:55 | Guarda el snapshot de la cartera y recalcula el riesgo |
| `ETFService` | al arrancar y a las 02:00 | Da de alta los ETFs y acciones configurados que falten |

### Alerta de caída

```
  Scheduler (cada 5 min)
       │
       ▼
  CoinGecko / Yahoo ──► ¿precio actual vs precio anterior?
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

### Alerta de precio

```
  PriceAlertScheduler (cada 5 min)
       │
       ▼
  Último precio del activo ──► ¿cruza el objetivo (above / below)?
                                   │
                       ┌───────────┴───────────┐
                      NO                       SÍ
                       │                       │
                  Sigue activa       📱 Telegram + alerta desactivada
```

El umbral de caída se configura con `sentinel.alert.threshold` (por defecto `-0.05`, es decir, −5 %).

---

## 🧪 Tests

```bash
./mvnw test
```

Tests unitarios (servicios, controladores, schedulers, parseo de Yahoo) e integración con H2 en memoria, incluida la protección CSRF. Los tests no hacen llamadas a APIs externas.

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
