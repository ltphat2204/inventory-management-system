# Shared Kernel Module

The Shared Kernel is the technical backbone of the application. Unlike feature modules (which encapsulate specific business domains), the `shared` module contains cross-cutting concerns, utilities, and configurations that are used globally across all feature modules.

## Responsibilities and Features

This module is divided into three primary packages: **API**, **Config**, and **Security**.

### 1. Global API Standards (`api/`)
Ensures that the entire API communicates consistently with the frontend clients.
- **`ApiResponse.java`**: A standardized generic wrapper for all REST API endpoints. It ensures every endpoint returns a consistent JSON structure (e.g., success status, data payload, error messages).
- **`GlobalExceptionHandler.java`**: A centralized `@ControllerAdvice`. It intercepts exceptions thrown anywhere in the system (like custom domain exceptions from the IAM module or standard Spring exceptions) and maps them into a clean, predictable HTTP response.

### 2. Global Configuration (`config/`)
Contains system-wide Spring Boot configurations.
- **`JpaAuditingConfig.java`**: Configures JPA Auditing (often paired with Hibernate Envers). This automatically tracks who created or modified database records and when (`created_at`, `updated_at`).
- **`DataInitializer.java`**: A bootstrapping utility that runs when the application starts, typically used to seed the database with default roles (e.g., ADMIN, USER) or an initial super-admin account if none exists.

### 3. Security Infrastructure (`security/`)
Centralizes the stateless, JWT-based security configuration so that feature modules can simply use roles/permissions without writing security logic.
- **Spring Security Setup (`SecurityConfig.java`)**: Defines the global security filter chain, enabling CORS, disabling CSRF (for stateless REST APIs), and configuring endpoint access rules.
- **JWT Operations (`JwtTokenProvider.java` & `JwtAuthenticationFilter.java`)**: 
  - The provider generates and validates JWT tokens using the JJWT library.
  - The filter intercepts every incoming HTTP request, extracts the JWT from the `Authorization` header, validates it, and sets the authentication context for the request.
- **User Details Adapter (`CustomUserDetails` & `CustomUserDetailsService`)**: The bridge between Spring Security's required user models and the application's actual domain `User` model, used during token validation to load user roles and authorities.

## Usage Rules
1. **No Business Logic**: The shared module must **never** contain code specific to a single business feature (like inventory calculation or order processing).
2. **Dependency Direction**: Feature modules can depend on `shared`, but `shared` **must never** depend on a feature module. This prevents circular dependencies.
3. **Global Scope**: Only code that is truly generic and reusable should be placed in `shared`.
