# Inventory Module

The Inventory module manages stock state and inventory transactions for product variants. It follows Domain-Driven Design (DDD) with clear separation of presentation, application, domain, and infrastructure concerns.

---

## Features

- **Stock Import**: Import stock in batch with idempotency key protection.
- **Inventory Balance Tracking**: Persist and update quantity by `variantId`.
- **Inventory Transaction History**: Record movement events for auditing and reconciliation.
- **Movement Type Control**: Explicit movement type modeling for stock operations.
- **Cross-Module Integration**: Serves stock quantity data for Catalog use cases.

---

## API Endpoints

### Stock Imports

| Method | Path | Roles | Description |
|---|---|---|---|
| `POST` | `/api/v1/stock-imports` | Admin, Manager | Import stock in batch with idempotency protection (201) |

---

## Architecture Breakdown

### 1. Presentation Layer (`presentation/controller/`)
- `StockImportController` exposes stock import endpoint.
- Returns standard `ApiResponse<T>` envelope from shared module.

### 2. Application Layer (`application/`)
- **Services**:
  - `InventoryService` / `InventoryServiceImpl`
  - `StockImportService` / `StockImportServiceImpl`
- **DTOs**:
  - `StockImportRequest`, `StockImportItemRequest`
  - `StockImportResponse`, `StockImportItemResponse`

### 3. Domain Layer (`domain/`)
- **Models**: `Inventory`, `InventoryTransaction`, `StockImport`, `StockImportItem`, `MovementType`.
- **Repository Interfaces**: `InventoryRepository`, `InventoryTransactionRepository`, `StockImportRepository`.
- **Exceptions**: `InventoryNotFoundException`.

### 4. Infrastructure Layer (`infrastructure/persistence/`)
- **JPA Entities**: `JpaInventory`, `JpaInventoryTransaction`, `JpaStockImport`, `JpaStockImportItem`, `JpaMovementType`.
- **Spring Data Repositories**:
  - `SpringDataInventoryRepository`
  - `SpringDataInventoryTransactionRepository`
  - `SpringDataStockImportRepository`
- **Persistence Implementations**:
  - `InventoryRepositoryImpl`
  - `InventoryTransactionRepositoryImpl`
  - `StockImportRepositoryImpl`
- **Mappers**: `InventoryMapper`, `InventoryTransactionMapper`, `StockImportMapper`.

---

## Cross-Module Dependencies

| Dependency | Direction | Purpose |
|---|---|---|
| Catalog -> Inventory (`InventoryService`) | Catalog reads inventory quantity | Variant stock and low-stock signals |
| Shared `ApiResponse` | Inventory -> Shared | Unified response envelope |
| Shared exception/security config | Shared -> Inventory | Consistent error mapping and authorization |
