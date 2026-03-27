# Catalog Module

The Catalog module is responsible for managing the organization of products through categories. It follows Domain-Driven Design (DDD) principles to ensure clear separation between business logic, application use cases, and infrastructure details.

## Features

- **Category Management (`CategoryController`)**: 
  - List categories with support for pagination and sorting (Available to all authenticated users).
  - Create, update, and delete categories (Restricted to Admin role).
- **Validation**: Ensures that Vietnamese names are mandatory for every category.
- **Delete Constraints**: Prevent deletion of categories that have linked products (Stubbed until the Product entity is implemented).
- **Auditing**: Automatically tracks the creation timestamp for each category using JPA auditing/Envers.

---

## Architecture Breakdown

### 1. Presentation Layer (`presentation/controller/`)
- `CategoryController.java`: Entry point for all category-related operations. It uses the standard `ApiResponse` envelope.

  | Method | Path | Roles | Description |
  |---|---|---|---|
  | `GET` | `/api/v1/categories` | All Authenticated | Paginated list; supports `?page`, `?limit`, `?sort` |
  | `POST` | `/api/v1/categories` | Admin | Create new category (201) |
  | `GET` | `/api/v1/categories/{id}` | All Authenticated | Get single category details |
  | `PUT` | `/api/v1/categories/{id}` | Admin | Update existing category |
  | `DELETE` | `/api/v1/categories/{id}` | Admin | Delete category (if no products linked) |

### 2. Application Layer (`application/`)
- **Services**:
  - `CategoryService` / `CategoryServiceImpl`: Orchestrates category CRUD operations, handles pagination indexing (1-indexed API to 0-indexed Spring Data), and enforces business constraints like the "has products" check.
- **DTOs**:
  - Request: `CreateCategoryRequest`, `UpdateCategoryRequest`.
  - Response: `CategoryResponse`.

### 3. Domain Layer (`domain/`)
The core business logic, decoupled from frameworks and database details.
- **Models**: `Category` (POJO representing the business concept).
- **Repositories (Interfaces)**: `CategoryRepository` (Defines persistence contracts and business checks like `hasProducts`).
- **Exceptions**:
  - `CategoryNotFoundException` (Mapped to 404 Not Found).
  - `CategoryHasProductsException` (Mapped to 400 Bad Request).

### 4. Infrastructure Layer (`infrastructure/persistence/`)
Implementation details for data persistence.
- **Entities**: `JpaCategory` (Mapped to the `categories` table with `@Audited` support).
- **Spring Data Repositories**: `SpringDataCategoryRepository`.
- **Adapters**: `CategoryRepositoryImpl` (Implements the Domain `CategoryRepository` interface by delegating to Spring Data and using Mappers).
- **Mappers**: `CategoryMapper` (MapStruct interface for converting between Domain Models, JPA Entities, and Application DTOs).

---

## Interaction with Shared Kernel
The Catalog module leverages the following components from the `shared` module:
- **API Response**: All controllers return the unified `ApiResponse` format.
- **Global Exception Handling**: `GlobalExceptionHandler` centrally handles Catalog-specific exceptions to return structured error messages.
- **Security**: Endpoint access is controlled using standard Spring Security roles defined in the shared security context.
- **Auditing**: Uses the shared JPA auditing configuration to maintain the `created_at` records.
