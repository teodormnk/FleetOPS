# 🚗 FleetOPS - Cloud Native Fleet Management System

![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![Docker](https://img.shields.io/badge/docker-compose-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1-green)
![C++](https://img.shields.io/badge/C++-17-00599C)

**FleetOPS** este o aplicație distribuită de tip *Cloud-Native* pentru gestionarea și monitorizarea în timp real a unei flote de vehicule. Proiectul demonstrează o arhitectură de microservicii poliglotă (Java & C++), orchestrată complet prin Docker, punând accent pe scalabilitate, observabilitate și securitate.

---

## 🏗️ Arhitectura sistemului

Sistemul este modularizat în containere Docker interconectate:

| Serviciu | Tehnologie | Rol și descriere                                                                                      |
| :--- | :--- |:------------------------------------------------------------------------------------------------------|
| **Gateway** | **Java (Spring Boot)** | Punctul central de intrare. Gestionează API, WebSocket și publică evenimente în Message Broker.       |
| **Routing Service** | **C++ 17** | Microserviciu consumator. Ascultă coada de mesaje, calculează rute asincron și returnează rezultatul. |
| **Message Broker** | **RabbitMQ** | Asigură decuplarea serviciilor și comunicarea asincronă (Event-Driven).                               |
| **Database** | **PostgreSQL 15** | Stocare persistentă pentru utilizatori, vehicule și comenzi.                                          |
| **Observability** | **Grafana / Loki / Prometheus** | Stack complet de monitorizare: Vizualizare, Agregare Loguri și Colectare Metrici.                     |
| **Frontend** | **HTML5 / Leaflet.js** | Dashboard interactiv pentru vizualizarea flotei.                                                      |

---

## 🛠️ Tehnologii și standarde implementate

Acest proiect bifează cerințele unui mediu modern DevOps:

* **Orchestrare:** `docker-compose` pentru pornirea întregului stack (7 containere).
* **Event-Driven Architecture:** Comunicare asincronă între Java și C++ folosind **RabbitMQ** (înlocuiește HTTP sincron).
* **Security & Secret Management:** Credențialele injectate prin `.env`.
* **Advanced Observability:** Stack complet **Prometheus** (Metrici) + **Loki** (Loguri centralizate) + **Grafana** (Vizualizare Dashboard).
* **CI/CD & Security:** Pipeline GitHub Actions care include build, teste și **scanare de vulnerabilități cu Trivy**.

---

## 🚀 Instrucțiuni de instalare și pornire

### 1. Cerințe preliminare
* Docker Desktop instalat și pornit.
* Porturile următoare libere:
  * `8088` (App Gateway)
  * `5433` (Database)
  * `3000` (Grafana Dashboard)
  * `15672` (RabbitMQ Management)

### 2. Configurare secrete (obligatoriu)
Din motive de securitate, fișierul de configurare nu este inclus în repository.
Creați un fișier numit **`.env`** în rădăcina proiectului și adăugați următorul conținut:

```properties
# Configurare Bază de Date
DB_USER=fleet_admin
DB_PASSWORD=secret_secure_password
DB_NAME=fleet_ops_db
DB_PORT_EXTERNAL=5433

# Configurare Gateway
GATEWAY_PORT_EXTERNAL=8088

# Configurare Securitate (JWT)
JWT_SECRET=Cheie_Secreta_Foarte_Lunga_Si_Sigura_Pentru_Demo_2024
JWT_EXPIRATION_MS=86400000
```

### 3. Pornirea aplicației
Deschideți un terminal în folderul proiectului și rulați comanda unică de orchestrare:

```bash
docker compose up --build
```
> **Notă:** Așteptați până când vedeți log-ul: `Started GatewayApplication in ... seconds`.

## 🎮 Utilizare și endpoint-uri

Odată pornită aplicația, aveți acces la următoarele interfețe:

### 🌍 1. Dashboard vizual (Frontend)
* **Acces:** [http://localhost:8088](http://localhost:8088)
* Afișează o hartă (OpenStreetMap) cu pozițiile vehiculelor.
* Pozițiile se actualizează automat la fiecare 3 secunde (simulare server-side).

### 📑 2. Documentație API (Swagger UI)
* **Acces:** [http://localhost:8088/swagger-ui.html](http://localhost:8088/swagger-ui.html)
* Interfață grafică ce permite vizualizarea și testarea manuală a endpoint-urilor REST.

### 🔌 3. Endpoint-uri principale (REST)

* `GET /api/vehicles`: Listează flota curentă și statusul fiecărui vehicul.
* `POST /api/orders`: Creează o comandă și declanșează calculul rutei în microserviciul C++.

**Exemplu body JSON:**
```json
{
  "userId": 2,
  "pickupLocation": "Piata Sfatului",
  "destination": "Gara Brasov"
}
```

**Efect (Flux asincron):**
1.  Gateway-ul salvează comanda cu status `PROCESSING` și trimite un mesaj în coada `order.queue`.
2.  Utilizatorul primește răspuns imediat (`200 OK`), fără a aștepta calculul rutei (Non-blocking).
3.  Serviciul C++ preia mesajul, calculează ruta și trimite rezultatul în `order.route`.
4.  Gateway-ul consumă rezultatul și actualizează comanda în baza de date.

## 📊 4. Observabilitate Avansată

Sistemul expune un stack complet de monitorizare accesibil local:

### 📈 Grafana (Vizualizare & Loguri)
* **Acces:** [http://localhost:3000](http://localhost:3000)
* **User/Parolă:** `admin` / `admin` (puteți da skip la schimbarea parolei).
* **Ce puteți vedea:**
  1.  Mergeți la meniul **Explore** (busola din stânga).
  2.  Selectați sursa **Prometheus** pentru a vedea grafice (query: `fleet_routes_calculated_total`).
  3.  Selectați sursa **Loki** pentru a vedea logurile centralizate din toate containerele (label: `{app="fleet-gateway"}`).

### 🐰 RabbitMQ Management
* **Acces:** [http://localhost:15672](http://localhost:15672)
* **User/Parolă:** `guest` / `guest`
* **Funcționalitate:** Monitorizați cozile de mesaje (`order.queue`, `order.route`) și debitul de procesare în timp real.

### ❤️ Health Checks
* **API:** [http://localhost:8088/actuator/health](http://localhost:8088/actuator/health)

## ⚙️ Structura proiectului

```plaintext
fleet-ops-project/
├── .github/workflows/   # Pipeline CI/CD + Trivy Security Scan
├── observability/       # Configurare Prometheus
├── database/            # Scripturi SQL (Schema + Seed)
├── gateway/             # Aplicația Java (Producer/Consumer RabbitMQ)
│   ├── src/main/resources/logback-spring.xml # Configurare Loguri -> Loki
│   └── src/main/resources/static # Frontend
├── routing-service/     # Microserviciu C++ (RabbitMQ Client)
│   ├── src/             # Cod sursă C++
│   └── Dockerfile       # Multi-stage build (Alpine)
├── docker-compose.yml   # Orchestrare (App + Monitoring Stack)
└── .env                 # Fișier secrete (GitIgnored)
```

## 🧪 CI/CD Pipeline

Proiectul include un workflow automatizat (`.github/workflows/main.yml`) care rulează la fiecare push pe branch-ul `main`:

* **Build & Test Java:** Compilează Gateway-ul și rulează testele unitare cu Maven.
* **Docker Build:** Verifică dacă imaginile Docker (inclusiv compilarea C++) se construiesc corect.
* **Security Scan:** Scanează codul pentru vulnerabilități folosind Trivy.

Dezvoltat de: Mincă Teodor Andrei, Mincu Florin Adrian <br>
Grupa: 10LF342 <br>
Facultatea de Matematică și Informatică, Universitatea Transilvania din Brașov <br>
Proiect Arhitectură Cloud și DevOps