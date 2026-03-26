# IAM Module (Identity and Access Management)

The IAM module is a core feature module responsible for managing user identities, roles, and the initial authentication lifecycle. It strictly follows Domain-Driven Design (DDD) principles.

## Features

- **Authentication Endpoints (`AuthController`)**: Exposes APIs for user login, logout, and token refreshing.
- **Token Management**: Implements secure refresh token rotation (`RefreshToken` model, `IRefreshTokenRepository`).
- **User & Role Management**: Core domain logic for `User` and `Role` entities, ensuring only authorized personnel can access the system.
- **Custom Exceptions**: Defines domain-specific exceptions like `InvalidCredentialsException` and `TokenRefreshException` for precise error handling.

## Architecture Breakdown

### 1. Presentation Layer (`presentation/controller/`)
- `AuthController.java`: The entry point for login and token refresh requests. It maps incoming JSON requests to Application DTOs.

### 2. Application Layer (`application/`)
- **Services**: `IAuthService` and `AuthServiceImpl` orchestrate the login flow, verifying credentials and coordinating with the token provider (located in the `shared` module).
- **DTOs**: `LoginRequest`, `LogoutRequest`, `RefreshTokenRequest`, `AuthResponse`, and `UserDto` ensure that internal domain models are never exposed directly to the outside world.
- **Mappers**: `AuthApplicationMapper` handles conversions between Domain Models and DTOs.

### 3. Domain Layer (`domain/`)
The pure business logic, independent of any framework.
- **Models**: `User`, `Role`, and `RefreshToken`.
- **Repositories (Interfaces)**: `IUserRepository`, `IRoleRepository`, and `IRefreshTokenRepository` dictate the contract for persistence without knowing about the database.
- **Exceptions**: Specific business rule violations like `InvalidCredentialsException`.

### 4. Infrastructure Layer (`infrastructure/persistence/`)
The technical implementation connecting the domain to PostgreSQL.
- **Entities**: `JpaUser`, `JpaRole`, `JpaRefreshToken` map directly to database tables using Hibernate/JPA annotations.
- **Spring Data Repositories**: Data access interfaces (`SpringDataUserRepository`, etc.).
- **Adapters**: Classes like `UserRepositoryAdapter` implement the Domain's `IUserRepository` interface, bridging the domain and Spring Data.
- **Mappers**: MapStruct interfaces (`UserPersistenceMapper`, etc.) that convert JPA Entities to pure Domain Models.

## Interaction with Shared Kernel
While IAM handles the *login* process and manages user data, the actual generation of JWTs and the Spring Security filter chain reside in the `shared` module. IAM calls the `JwtTokenProvider` (from `shared`) to mint authentication tokens upon successful login.
