# Inventory Management System - Backend API

This repository contains the backend service for the Inventory Management System. It is built as a modular monolith using **Java 21**, **Spring Boot**, and follows **Domain-Driven Design (DDD)** principles to ensure maximum maintainability, separation of concerns, and scalability.

## Project Summary

The backend exposes a RESTful API serving the frontend client. It manages all core business logic, including identity and access management (IAM), inventory processing, logging, and data persistence using PostgreSQL and Hibernate/JPA.

### Core Technologies
- **Java 21**
- **Spring Boot** (Web, Data JPA, Security, Actuator, Validation)
- **PostgreSQL** (Database)
- **Hibernate Envers** (Data Auditing/Versioning)
- **JJWT** (JSON Web Token for Authentication)
- **MapStruct & Lombok** (Boilerplate reduction and object mapping)
- **Maven** (Build Tool)

---

## Organization & Architecture

The application structure is organized around **feature modules** (e.g., `iam`, `inventory`, etc.) rather than technical layers. This approach is often called "Package by Feature" or a "Modular Monolith". 

The codebase is partitioned into feature modules and a shared kernel:

```text
src/main/java/ltphat/inventory/backend/
├── shared/                  <-- Shared Kernel (Cross-cutting concerns)
│   ├── api/                 <-- Global API Response/Exception handling
│   ├── config/              <-- Global Spring configurations
│   └── security/            <-- JWT and Security filter chain
│
└── [feature]/               <-- Self-contained business modules
    ├── domain/              <-- Business logic & Domain models
    ├── application/         <-- Use cases & Services
    ├── infrastructure/      <-- Persistence & External adapters
    └── presentation/        <-- REST Controllers & DTOs
```

- **Shared Kernel (`shared/`)**: Manages the technical infrastructure shared by all modules.
- **Feature Modules**: Encapsulate specific business domains, ensuring that changes in one module (like `inventory`) have minimal impact on others (like `iam`).

---

## The Domain-Driven Design (DDD) Method

Each feature module is internally structured following strict DDD layers. This isolates the core business logic from external frameworks, databases, and delivery mechanisms (like HTTP).

A typical module (e.g., `iam`) contains the following layers:

### 1. `domain/` (The Core)
This is the heart of the module. It has **no dependencies** on Spring or external libraries (except basic Java).
*   **`model/`**: Pure Java objects representing business domain concepts.
*   **`repository/`**: Interfaces defining how domain models are persisted and retrieved. The implementation is left to the infrastructure layer.
*   **`exception/`**: Domain-specific business rule violations.

### 2. `application/` (The Use Cases)
Orchestrates the business logic using the domain objects. It does not contain complex business rules but coordinates tasks.
*   **`service/` & `service/impl/`**: Interfaces and their implementations that define the use cases of the system.
*   **`dto/`**: Data Transfer Objects used to pass data in and out of the application services without exposing internal domain models.

### 3. `infrastructure/` (The Technical Details)
Contains all implementation details related to databases, external services, and frameworks.
*   **`persistence/`**: Everything related to the database.
    *   **`entity/`**: JPA Entities (e.g., `@Entity`, `@Table`) mapped to database tables.
    *   **`repository/`**: Spring Data JPA repository implementations of the interfaces defined in the `domain` layer.
    *   **`mapper/`**: MapStruct interfaces that convert between JPA `Entities` and Domain `Models`.

### 4. `presentation/` (The Delivery Mechanism)
The entry point for external communication.
*   **`controller/`**: Spring `@RestController` classes that receive HTTP requests, map them to Application DTOs, invoke Application Services, and return HTTP responses.

---

## How to Implement a New Feature/Module

Follow these steps when creating a new module (e.g., adding an `orders` feature):

1. **Create the Module Folder**: Inside `src/main/java/ltphat/inventory/backend/`, create a new package named `orders`.
2. **Define the Domain**:
   - Create `orders/domain/model/Order.java` (POJO, no JPA annotations).
   - Create `orders/domain/repository/OrderRepository.java` (Interface).
3. **Build the Use Cases (Application)**:
   - Create `orders/application/dto/CreateOrderRequest.java` and `OrderResponse.java`.
   - Create `orders/application/service/OrderService.java`.
   - Implement it in `orders/application/service/impl/OrderServiceImpl.java`, injecting the `OrderRepository` interface.
4. **Implement Infrastructure (Persistence)**:
   - Create the database table representation: `orders/infrastructure/persistence/entity/OrderJpaEntity.java` (using `@Entity`).
   - Create the Spring Data interface: `orders/infrastructure/persistence/repository/SpringDataOrderRepository.java`.
   - Implement the Domain repository: Create `OrderRepositoryImpl.java` that implements the domain's `OrderRepository` and delegates to the `SpringDataOrderRepository`.
   - Create MapStruct mappers in `orders/infrastructure/persistence/mapper/` to map `OrderJpaEntity` <-> `Order` domain model.
5. **Expose the API (Presentation)**:
   - Create `orders/presentation/controller/OrderController.java`. Inject the `OrderService` and map HTTP endpoints to the service methods.

## Current Feature Implementation

The system currently implements the following core modules and technical capabilities:

### Identity and Access Management (IAM)
The IAM module provides a secure foundation for user authentication and resource authorization.
- **Authentication Flow**: Implements stateless authentication using JSON Web Tokens (JWT). It includes secure login endpoints with BCrypt password hashing.
- **Token Management**: Handles JWT generation, extraction, and validation through a custom security filter chain.
- **Role-Based Access Control (RBAC)**: Supports defining user roles to restrict access to specific API endpoints.
- **User Persistence**: Manages the lifecycle of user accounts and their associated security credentials.

### Catalog Management
The Catalog module allows for the organization and management of product categories.
- **Category CRUD**: Full support for creating, reading (individual and paginated), updating, and deleting categories.
- **Delete Constraints**: Prevents deletion of categories that are linked to products (currently enforced via a placeholder check).
- **Localized Naming**: Supports both Vietnamese (`name_vn`) and English (`name_en`) naming for categories.

### Shared Infrastructure
The shared kernel provides a standardized interface for the entire application.
- **Standardized API Responses**: A unified `ApiResponse` wrapper ensures consistency across all REST endpoints.
- **Global exception handling**: A centralized `GlobalExceptionHandler` intercepts business and technical exceptions to return meaningful, structured error messages to the client.
- **Security Configuration**: Centralized Spring Security setup including CORS policies and the JWT authentication entry point.
- **Data Auditing**: Integration with Hibernate Envers to provide automated versioning and auditing for database entities.
