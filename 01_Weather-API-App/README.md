# 01 Weather Data Management System (RESTful API)

### **Executive Summary**
This project is a multi-module Java Spring Boot application designed to manage real-time weather data ingestion and client access. It serves as technical evidence of proficiency in backend engineering, modular software architecture, and secure API design.

---

## 🛠 Technology Stack
* **Language:** Java 17
* **Framework:** Spring Boot 3.x (Data JPA, Web, Security)
* **Build Tool:** Maven (Multi-module Architecture)
* **Database:** MySQL (Relational Schema)
* **Security:** Custom Security Filter (Client ID/Secret validation)
* **Documentation:** Swagger UI / OpenAPI 3.0

---

## 🚀 Key Features
* **Multi-Module Architecture:** Decoupled system design using `Common`, `Service`, and `ClientManager` modules to ensure high maintainability and separation of concerns.
* **Custom Security Infrastructure:** Engineered a specialized security filter layer to validate administrative and weather station clients via unique Client ID and Client Secret credentials.
* **Real-time Data Ingestion:** Optimized REST endpoints designed for high-frequency data updates from automated weather stations.
* **Administrative Control:** Full CRUD (Create, Read, Update, Delete) capabilities for managing geographical locations and authorized client applications.

---

## 📂 Project Structure
* **`WeatherApiCommon`**: Houses shared entities, Data Transfer Objects (DTOs), and relational mapping for the MySQL database.
* **`WeatherApiService`**: The primary engine containing REST controllers and business logic layers.
* **`WeatherApiClientManager`**: A dedicated module for administrative lifecycle management and credential issuing.

---

## 📝 API Documentation (Swagger)
This project utilizes Swagger UI to provide an interactive interface for real-time API testing and developer documentation.
* **Local Endpoint:** [Weather Data Management System API Document](https://app.swaggerhub.com/apis/MALACHIJAMISON2/WeatherAPI/1.0.0)
* **Scope:** Includes full documentation for all endpoints covering:
    * **Location Management:** Global coordinates and regional data.
    * **Weather Metrics:** Real-time temperature, humidity, and status updates.
    * **Client Management:** Registration and authentication of API consumers.

---

## ⚙️ Project Setup
1.  **Clone the Repository:**
    ```bash
    git clone [https://github.com/malachi-jamison/FA26A-Technical-Portfolio.git](https://github.com/malachi-jamison/FA26A-Technical-Portfolio.git)
    ```
2.  **Database Configuration:**
    * Create a MySQL database named `weatherdb`.
    * Configure `src/main/resources/application.properties` with local credentials or utilize the secure environment variable method: `${DB_PASSWORD}`.
3.  **Build & Run:**
    * Execute `mvn clean install` from the project root.
    * Launch via Spring Tool Suite (STS) or run `mvn spring-boot:run`.

---
