# Inventory Module

The Inventory module manages stock state and inventory transactions for product variants. It follows Domain-Driven Design (DDD) with clear separation of presentation, application, domain, and infrastructure concerns.

---

## Features

- **Inventory Overview**: Global paginated inventory list with low-stock and product filters.
- **Stock Import**: Import stock in batch with idempotency key protection.
- **Inventory Balance Tracking**: Persist and update quantity by `variantId`.
- **Inventory Transaction History**: Record movement events for auditing and reconciliation.
- **Movement Type Control**: Explicit movement type modeling for stock operations.
- **Cross-Module Integration**: Serves stock quantity data for Catalog use cases.

---

## API Endpoints

### Inventory Overview

| Method | Path | Roles | Description |
|---|---|---|---|
| `GET` | `/api/v1/inventory` | Any authenticated user | Global inventory list with pagination, sorting, and optional filters |

#### Query Parameters

- `page` (default `1`): 1-based page index.
- `limit` (default `20`): page size.
- `lowStockOnly` (optional): when `true`, only returns rows where `currentQuantity < lowStockThreshold`.
- `productId` (optional): filter by product.
- `sort` (optional): supports `currentQuantity`, `variantSku`, `productName`, `lowStockThreshold`; prefix with `-` for descending.

#### Response Fields

- `variantId`
- `variantSku`
- `productId`
- `productName`
- `currentQuantity`
- `lowStockThreshold`
- `lowStock`

### Stock Imports

| Method | Path | Roles | Description |
|---|---|---|---|
| `POST` | `/api/v1/stock-imports` | Admin, Manager | Import stock in batch with idempotency protection (201) |

---

## Architecture Breakdown

### 1. Presentation Layer (`presentation/controller/`)
- `InventoryController` exposes inventory overview endpoint (`GET /inventory`).
- `StockImportController` exposes stock import endpoint.
- Returns standard `ApiResponse<T>` envelope from shared module.

### 2. Application Layer (`application/`)
- **Services**:
  - `InventoryService` / `InventoryServiceImpl`
  - `StockImportService` / `StockImportServiceImpl`
- **DTOs**:
  - `InventoryOverviewResponse`
  - `StockImportRequest`, `StockImportItemRequest`
  - `StockImportResponse`, `StockImportItemResponse`

### 3. Domain Layer (`domain/`)
- **Models**: `Inventory`, `InventoryOverview`, `InventoryTransaction`, `StockImport`, `StockImportItem`, `MovementType`.
- **Repository Interfaces**: `InventoryRepository`, `InventoryTransactionRepository`, `StockImportRepository`.
- **Exceptions**: `InventoryNotFoundException`.

### 4. Infrastructure Layer (`infrastructure/persistence/`)
- **JPA Entities**: `JpaInventory`, `JpaInventoryTransaction`, `JpaStockImport`, `JpaStockImportItem`, `JpaMovementType`.
- **Spring Data Repositories**:
  - `SpringDataInventoryRepository`
  - `SpringDataInventoryTransactionRepository`
  - `SpringDataStockImportRepository`
- **Projections**:
  - `InventoryOverviewProjection` (optimized joined read for overview endpoint)
- **Persistence Implementations**:
  - `InventoryRepositoryAdapter`
  - `InventoryTransactionRepositoryAdapter`
  - `StockImportRepositoryAdapter`
- **Mappers**: `InventoryMapper`, `InventoryTransactionMapper`, `StockImportMapper`.

### 5. Database Indexes

The inventory table defines explicit indexes used by the overview endpoint:

- `idx_inventory_variant_id` on `inventory.variant_id`
- `idx_inventory_current_quantity` on `inventory.current_quantity`

---

## Cross-Module Dependencies

| Dependency | Direction | Purpose |
|---|---|---|
| Catalog -> Inventory (`InventoryService`) | Catalog reads inventory quantity | Variant stock and low-stock signals |
| Shared `ApiResponse` | Inventory -> Shared | Unified response envelope |
| Shared exception/security config | Shared -> Inventory | Consistent error mapping and authorization |
