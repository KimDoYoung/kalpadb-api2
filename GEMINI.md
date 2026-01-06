# KalpaDB RESTful API Context

## Project Overview

This project is a RESTful API service for the **KalpaDB**, built using **Spring Boot**. It serves as the backend for client applications, specifically mentioned is **PCMS2**.

The system is designed to expose data from a **MariaDB** (referenced as compatible with MySQL) database via secure REST endpoints protected by **JWT Authentication**.

**개발자는 한국인이다. 그래서 한글을 위주로 대화한다**

### Key Technologies

* **Language:** Java 21
* **Framework:** Spring Boot 4.0.1 (Note: `build.gradle` specifies this version, though typically Spring Boot is in 3.x series currently. Ensure version compatibility).
* **Build Tool:** Gradle 8.11
* **Database:** MariaDB 10.3.35 (Driver: `mariadb-java-client`)
* **Authentication:** JWT (JSON Web Tokens)
* **Testing:** JUnit 5, RestAssured, Jacoco

## Architecture & Structure

The project follows a standard layered Spring Boot architecture:

* **`src/main/java/kr/co/kalpa/dbapi2/`**: Root package.
  * **`config/`**: Configuration classes (Security, JPA, Web).
  * **`controller/`**: REST API endpoints.
  * **`service/`**: Business logic layer.
  * **`repository/`**: Data access layer (JPA Repositories).
  * **`entity/`**: JPA Entities mapping to DB tables.
  * **`dto/`**: Data Transfer Objects (`request/`, `response/`).
  * **`security/`**: JWT authentication and authorization logic.
  * **`exception/`**: Global exception handling.
  * **`util/`**: Helper classes.

## Development Workflow

### Build & Run

* **Build Project:**

    ```bash
    ./gradlew build
    ```

* **Run Application (Dev Profile):**

    ```bash
    ./gradlew bootRun -Pprofile=dev
    ```

* **Run Application (Prod Profile):**

    ```bash
    ./gradlew bootRun -Pprofile=prod
    ```

* **Clean Build:**

    ```bash
    ./gradlew clean
    ```

### Testing

* **Run All Tests:**

    ```bash
    ./gradlew test
    ```

* **Run Specific Test:**

    ```bash
    ./gradlew test --tests <ClassName>
    # Example: ./gradlew test --tests UserControllerTest
    ```

* **Generate Coverage Report:**

    ```bash
    ./gradlew test jacocoTestReport
    ```

    Reports found at: `build/reports/jacoco/test/html/index.html`

### Development Guidelines

1. **New Feature/Entity Checklist:**
    * Define **Entity** in `entity/`.
    * Create **Repository** interface in `repository/`.
    * Define **Request/Response DTOs** in `dto/`.
    * Implement **Service** logic in `service/`.
    * Expose endpoints via **Controller** in `controller/`.
    * Add **Tests** in `src/test/`.

2. **Database Configuration:**
    * Update `src/main/resources/application.properties` or profile-specific files (`application-dev.properties`).
    * Standard URL format: `jdbc:mariadb://<host>:3306/kalpadb`.

3. **Code Style:**
    * Use **Lombok** annotations (`@Data`, `@Builder`, etc.) to reduce boilerplate.
    * Follow standard Java naming conventions.

## Key Files

* **`build.gradle`**: Project dependencies and build configuration.
* **`README.md`**: Project documentation and setup guide.
* **`src/main/resources/application.properties`**: Main configuration file.
* **`docs/kalpadb_schema.sql`**: Database schema definition.
