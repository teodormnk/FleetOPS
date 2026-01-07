# 🚗 FleetOPS - Cloud Native Fleet Management System

![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![Docker](https://img.shields.io/badge/docker-compose-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1-green)
![C++](https://img.shields.io/badge/C++-17-00599C)

**FleetOPS** este o aplicație distribuită de tip *Cloud-Native* pentru gestionarea și monitorizarea în timp real a unei flote de vehicule. Proiectul demonstrează o arhitectură de microservicii poliglotă (Java & C++), orchestrată complet prin Docker, punând accent pe scalabilitate, observabilitate și securitate.

---

## 🏗️ Arhitectura Sistemului

Sistemul este modularizat în containere Docker interconectate:

| Serviciu | Tehnologie | Rol și Descriere |
| :--- | :--- | :--- |
| **Gateway** | **Java (Spring Boot)** | Punctul central de intrare. Gestionează API-ul REST, conexiunile WebSocket, securitatea și comunicarea cu baza de date. |
| **Routing Service** | **C++ 17** | Microserviciu de calcul intensiv (High-Performance). Calculează rute și distanțe folosind algoritmi geometrici, expunând un API HTTP intern. |
| **Database** | **PostgreSQL 15** | Stocare persistentă pentru utilizatori, vehicule și comenzi. Include un seed inițial de date. |
| **Frontend** | **HTML5 / Leaflet.js** | Dashboard interactiv pentru vizualizarea pozițiilor pe hartă în timp real (prin WebSocket). |

---

## 🛠️ Tehnologii și Standarde Implementate

Acest proiect bifează cerințele unui mediu modern DevOps:

* [cite_start]**Orchestrare:** `docker-compose` pentru pornirea întregului stack[cite: 59].
* [cite_start]**API Gateway & WebSocket:** Spring Boot cu documentație **OpenAPI / Swagger**[cite: 42, 56].
* [cite_start]**Polyglot Microservices:** Integrare HTTP sincronă între Java și C++[cite: 50].
* [cite_start]**Security & Secret Management:** Credențialele nu sunt stocate în cod, ci injectate prin variabile de mediu (`.env`)[cite: 62].
* [cite_start]**Observabilitate:** Health Checks, Loguri structurate și metrici **Prometheus** custom (`fleet.routes.calculated`)[cite: 65, 108].
* [cite_start]**CI/CD:** Pipeline automatizat prin **GitHub Actions** (Build, Test, Docker packaging)[cite: 69].

---

## 🚀 Instrucțiuni de Instalare și Pornire

### 1. Cerințe Preliminare
* Docker Desktop instalat și pornit.
* Porturile `8088` și `5433` libere pe mașina locală.

### 2. Configurare Secrete (Obligatoriu)
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

### 3. Pornirea Aplicației
Deschideți un terminal în folderul proiectului și rulați comanda unică de orchestrare:

```bash
docker compose up --build
```
> **Notă:** Așteptați până când vedeți log-ul: `Started GatewayApplication in ... seconds`.

## 🎮 Utilizare și Endpoint-uri

Odată pornită aplicația, aveți acces la următoarele interfețe:

### 🌍 1. Dashboard Vizual (Frontend)
* **Acces:** [http://localhost:8088](http://localhost:8088)
* Afișează o hartă (OpenStreetMap) cu pozițiile vehiculelor.
* Pozițiile se actualizează automat la fiecare 3 secunde (simulare server-side).

### 📑 2. Documentație API (Swagger UI)
* **Acces:** [http://localhost:8088/swagger-ui.html](http://localhost:8088/swagger-ui.html)
* Interfață grafică ce permite vizualizarea și testarea manuală a endpoint-urilor REST.

### 🔌 3. Endpoint-uri Principale (REST)

* `GET /api/vehicles`: Listează flota curentă și statusul fiecărui vehicul.
* `POST /api/orders`: Creează o comandă și declanșează calculul rutei în microserviciul C++.

**Exemplu Body JSON:**
```json
{
  "userId": 2,
  "pickupLocation": "Piata Sfatului",
  "destination": "Gara Brasov"
}
```

**Efect:**

* Gateway-ul (Java) trimite coordonatele la serviciul de rutare (C++).
* Serviciul C++ returnează distanța și punctele rutei.
* Gateway-ul salvează comanda și incrementează metrica de monitoring.

## 📊 4. Observabilitate

* **Health Check:** `http://localhost:8088/actuator/health`
  * Verifică starea serviciilor (ex: conexiunea la baza de date).
* **Prometheus Metrics:** `http://localhost:8088/actuator/prometheus`
  * Căutați metrica specifică: `fleet_routes_calculated_total`.

## ⚙️ Structura Proiectului

```plaintext
fleet-ops-project/
├── .github/workflows/   # Pipeline CI/CD (GitHub Actions)
├── database/            # Scripturi SQL (Schema + Seed)
├── gateway/             # Aplicația principală (Spring Boot)
│   ├── src/main/java    # Cod sursă Java
│   └── src/main/resources/static # Frontend (HTML/JS)
├── routing-service/     # Microserviciu C++
│   ├── src/             # Cod sursă C++
│   └── Dockerfile       # Multi-stage build (Alpine)
├── docker-compose.yml   # Orchestrare servicii
└── .env                 # Fișier secrete (GitIgnored)
```

## 🧪 CI/CD Pipeline

Proiectul include un workflow automatizat (`.github/workflows/main.yml`) care rulează la fiecare push pe branch-ul `main`:

* **Build & Test Java:** Compilează Gateway-ul și rulează testele unitare cu Maven.
* **Docker Build:** Verifică dacă imaginile Docker (inclusiv compilarea C++) se construiesc corect.
* **Security Scan (Opțional):** Scanează codul pentru vulnerabilități folosind Trivy.

Dezvoltat de: Minca Teodor Andrei, Mincu Florin Adrian
Grupa: 10LF342
Facultatea de Matematică și Informatică, Universitatea Transilvania din Brașov
Proiect Arhitectură Cloud și DevOps