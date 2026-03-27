# Catalog Module

The Catalog module manages categories, products, and product variants (size/color/design matrix). It follows Domain-Driven Design (DDD) principles with a strict separation between business logic, application use cases, and infrastructure details. Stock quantity data is sourced from the **Inventory** module via an internal service interface.

---

## Features

- **Category Management**: Full CRUD with a guard preventing deletion of categories that have linked products.
- **Product Management**: Create products with a full variant matrix; auto-generate SKUs; soft-delete with stock = 0 guard.
- **Variant Management**: Add, update, and remove individual variants; enforce uniqueness per `(product_id, size, color, design_style)`.
- **Product List**: Paginated, filterable (`categoryId`, `isActive`, `q` for code/name search) product listing with `variantCount` summary.
- **Variant Stock Matrix**: Real-time `currentQuantity` and `lowStock` flag per variant, sourced from the Inventory module.
- **Barcode Lookup**: Single-endpoint lookup by barcode returning variant details, stock, `lowStock`, and `productNameVn`.
- **Auditing**: JPA Envers (`@Audited`) on all entities for change history.
- **DB Indexes**: `product_code`, `name_vn`, and `is_active` on the `products` table for efficient search.

---

## API Endpoints

### Categories

| Method | Path | Roles | Description |
|---|---|---|---|
| `GET` | `/api/v1/categories` | All Authenticated | Paginated list with `?page`, `?limit`, `?sort` |
| `POST` | `/api/v1/categories` | Admin | Create category (201) |
| `GET` | `/api/v1/categories/{id}` | All Authenticated | Get single category |
| `PUT` | `/api/v1/categories/{id}` | Admin | Update category |
| `DELETE` | `/api/v1/categories/{id}` | Admin | Delete (fails if products linked) |

### Products

| Method | Path | Roles | Description |
|---|---|---|---|
| `GET` | `/api/v1/products` | All Authenticated | Paginated list; `?page`, `?size`, `?sort`, `?categoryId`, `?isActive`, `?q` |
| `POST` | `/api/v1/products` | Admin | Create product + full variant matrix (201) |
| `GET` | `/api/v1/products/{id}` | All Authenticated | Product detail with variants, stock, and `lowStock` flag |
| `PUT` | `/api/v1/products/{id}` | Admin | Update product fields |
| `DELETE` | `/api/v1/products/{id}` | Admin | Soft-deactivate (fails if any variant has stock > 0) |

### Variants

| Method | Path | Roles | Description |
|---|---|---|---|
| `GET` | `/api/v1/products/{productId}/variants` | All Authenticated | Full variant stock matrix; `?includeStock=true` (default) |
| `POST` | `/api/v1/products/{productId}/variants` | Admin | Add single variant, initializes stock = 0 (201) |
| `GET` | `/api/v1/products/{productId}/variants/{variantId}` | All Authenticated | Single variant with stock |
| `PUT` | `/api/v1/products/{productId}/variants/{variantId}` | Admin | Update variant |
| `DELETE` | `/api/v1/products/{productId}/variants/{variantId}` | Admin | Remove variant (fails if stock > 0) |
| `GET` | `/api/v1/variants/barcode/{barcode}` | All Authenticated | Barcode/QR lookup; returns variant + `productNameVn` + stock + `lowStock` |

---

## Architecture Breakdown

### 1. Presentation Layer (`presentation/controller/`)
- `CategoryController` — category CRUD.
- `ProductController` — product CRUD and list.
- `ProductVariantController` — variant CRUD and barcode lookup.

All controllers return the unified `ApiResponse<T>` envelope from the shared module.

### 2. Application Layer (`application/`)
- **Services**:
  - `CategoryService` / `CategoryServiceImpl` — category CRUD with pagination and "has products" guard.
  - `ProductService` / `ProductServiceImpl` — product CRUD; builds variant list, auto-generates SKUs, delegates stock initialization to `InventoryService`.
  - `ProductVariantService` / `ProductVariantServiceImpl` — variant CRUD; fetches real-time stock and computes `lowStock`; populates `productNameVn` on barcode lookup.
- **DTOs**:
  - Requests: `CreateCategoryRequest`, `UpdateCategoryRequest`, `CreateProductRequest`, `VariantDto`.
  - Responses: `CategoryResponse`, `ProductResponse` (includes `variantCount`), `VariantResponse` (includes `currentQuantity`, `lowStock`, `productNameVn`).

### 3. Domain Layer (`domain/`)
- **Models**: `Category`, `Product`, `ProductVariant` (plain Java objects, no framework dependencies).
- **Repository Interfaces**: `CategoryRepository`, `ProductRepository`, `ProductVariantRepository`.
- **Exceptions**:
  - `CategoryNotFoundException` → 404
  - `CategoryHasProductsException` → 400
  - `ProductNotFoundException` → 404
  - `VariantNotFoundException` → 404
  - `DuplicateProductCodeException` → 400
  - `DuplicateVariantSkuException` → 400

### 4. Infrastructure Layer (`infrastructure/persistence/`)
- **JPA Entities**: `JpaCategory`, `JpaProduct` (with indexes on `product_code`, `name_vn`, `is_active`), `JpaProductVariant`.
- **Spring Data Repositories**: `SpringDataCategoryRepository`, `SpringDataProductRepository` (extends `JpaSpecificationExecutor` for dynamic filtering), `SpringDataProductVariantRepository`.
- **Adapters**: `CategoryRepositoryImpl`, `ProductRepositoryImpl`, `ProductVariantRepositoryImpl`.
- **Mappers**: `CategoryMapper`, `ProductMapper` (MapStruct).

---

## Cross-Module Dependencies

| Dependency | Direction | Purpose |
|---|---|---|
| `inventory.InventoryService` | Catalog → Inventory | Read `currentQuantity`; initialize stock on variant creation |
| `shared.ApiResponse` | Catalog → Shared | Unified HTTP response envelope |
| `shared.GlobalExceptionHandler` | Shared → Catalog | Maps domain exceptions to HTTP error responses |
| `shared.security` | Shared → Catalog | JWT-based role enforcement (`ADMIN`, authenticated) |
