# IAM Module (Identity and Access Management)

The IAM module is responsible for managing user identities, roles, and the authentication lifecycle. It strictly follows Domain-Driven Design (DDD) principles.

## Features

- **Authentication Endpoints (`AuthController`)**: Exposes APIs for login, logout, and token refreshing.
- **Secure Token Management**: Refresh token rotation, device fingerprint binding, and IP anomaly detection.
- **User Management Endpoints (`UserController`)**: Admin-only CRUD for users — list (paginated + filtered), create, get, update, soft-deactivate, and password reset.
- **RBAC Enforcement**: Every user management endpoint is guarded by `@PreAuthorize("hasRole('ADMIN')")` via Spring Method Security.
- **User & Role Domain Models**: Core domain logic for `User`, `Role`, and `RefreshToken`.
- **Custom Exceptions**: Domain-specific exceptions for precise error handling — authentication (`InvalidCredentialsException`, `TokenRefreshException`, `TokenSecurityException`) and user management (`UserNotFoundException`, `DuplicateUsernameException`, `RoleNotFoundException`).

---

## Architecture Breakdown

### 1. Presentation Layer (`presentation/controller/`)
- `AuthController.java`: Entry point for login, refresh, and logout. Reads `X-Device-Id` from request headers and manages the `refreshToken` HttpOnly cookie.

  | Method | Path | Description |
  |---|---|---|
  | `POST` | `/api/v1/auth/login` | Authenticate and receive access token + refresh token cookie |
  | `POST` | `/api/v1/auth/refresh` | Rotate refresh token and return new access token |
  | `POST` | `/api/v1/auth/logout` | Invalidate refresh token and clear cookie |
- `UserController.java`: Entry point for all user management operations. All six endpoints are annotated with `@PreAuthorize("hasRole('ADMIN')")` and return the standard `ApiResponse` envelope.

  | Method | Path | Description |
  |---|---|---|
  | `GET` | `/api/v1/users` | Paginated list; supports `?page`, `?limit`, `?sort`, `?role`, `?isActive` |
  | `POST` | `/api/v1/users` | Create user (201) |
  | `GET` | `/api/v1/users/{id}` | Get single user |
  | `PUT` | `/api/v1/users/{id}` | Full update |
  | `DELETE` | `/api/v1/users/{id}` | Soft-deactivate (`isActive=false`) |
  | `POST` | `/api/v1/users/{id}/reset-password` | Admin password reset |

### 2. Application Layer (`application/`)
- **Services**:
  - `IAuthService` / `AuthServiceImpl`: Orchestrate login, token issuance, rotation, and security checks.
  - `IUserManagementAppService` / `UserManagementAppServiceImpl`: Orchestrate user CRUD — enforces unique username check, hashes passwords via `BCryptPasswordEncoder`, resolves roles, and delegates persistence to the domain repository.
- **DTOs** (request): `LoginRequest`, `RefreshTokenRequest`, `CreateUserRequest`, `UpdateUserRequest`, `ResetPasswordRequest`.
- **DTOs** (response): `AuthResponse`, `AuthResult`, `UserDto` (used in login payload), `UserDetailDto` (used in user management), `RoleDto`.
- **Commands** (`domain/command/`): `CreateUserCommand`, `UpdateUserCommand` — plain Java value objects that carry validated, encoded data into persistence without coupling the domain to HTTP or Spring.
- **Mappers**: `AuthApplicationMapper` converts `User` → `UserDto` and `User` → `UserDetailDto`, and `Role` → `RoleDto`.

### 3. Domain Layer (`domain/`)
The pure business logic, independent of any framework.
- **Models**: `User` (has `isActive`, `email`, `createdAt`, `updatedAt`), `Role`, and `RefreshToken` (carries `deviceId`, `lastIp`, `lastUserAgent`, `lastUsedAt`).
- **Repositories (Interfaces)**: `IUserRepository` (includes paginated `findAll` with optional `roleName`/`isActive` filters, and `existsByUsernameAndIdNot`), `IRoleRepository`, `IRefreshTokenRepository`.
- **Exceptions**:
  - Authentication: `InvalidCredentialsException`, `TokenRefreshException`, `TokenSecurityException`.
  - User management: `UserNotFoundException`, `RoleNotFoundException`, `DuplicateUsernameException`.

### 4. Infrastructure Layer (`infrastructure/persistence/`)
Connects the domain to PostgreSQL.
- **Entities**: `JpaUser` (has `is_active`, `created_at`, `updated_at` via `@EntityListeners(AuditingEntityListener.class)`), `JpaRole`, `JpaRefreshToken`.
- **Spring Data Repositories**: `SpringDataUserRepository` (extends `JpaSpecificationExecutor` for dynamic filtering), `SpringDataRefreshTokenRepository`, `SpringDataRoleRepository`.
- **Adapters**: `UserRepositoryAdapter` (builds `Specification<JpaUser>` from optional `roleName`/`isActive` query params), `RefreshTokenRepositoryAdapter`.
- **Mappers**: MapStruct interfaces (`UserPersistenceMapper`, `RefreshTokenPersistenceMapper`).

---

## Interaction with Shared Kernel
IAM handles the login process and user data management. JWT generation and the Spring Security filter chain reside in the `shared` module. IAM calls `JwtTokenProvider` (from `shared`) to mint access tokens upon successful login.

---

## Security: Token Theft Edge Cases

The system implements five layered defenses. Below are the two primary theft scenarios and how each is handled.

### Case 1: Access Token Stolen

**Scenario**: An attacker intercepts or exfiltrates a valid JWT access token (e.g., through a compromised network or application log).

**Impact window**: Limited. Access tokens expire after **15 minutes** (`jwt.access.expiration=900000`). The attacker can only make API calls within this window.

**Mitigations in place**:
- **Short expiry (Defense 3)**: The token becomes useless in at most 15 minutes without any server-side action required.
- **No refresh capability**: Access tokens alone cannot be used to obtain a new access or refresh token. The attacker is stuck until expiry.
- **HttpOnly cookie (Defense 5)**: The refresh token is never accessible to JavaScript, so XSS attacks that steal the access token from memory cannot also steal the refresh token.

**Gap**: There is no mechanism to revoke a specific access token before expiry (stateless by design). If immediate revocation is required in the future, a short-lived deny-list (e.g., Redis) can be introduced.

---

### Case 2: Refresh Token Stolen

**Scenario**: An attacker obtains a valid refresh token (e.g., via a compromised device, MITM on HTTP, or a server-side data breach).

The attacker's goal is to call `POST /auth/refresh` to continuously mint new access tokens and maintain a persistent session.

**Defense 1 — Device Fingerprint Mismatch**:
The attacker operates from a different device/browser than the legitimate user. The stored `deviceId` in the refresh token record will not match the `X-Device-Id` header sent by the attacker.

```
Server: stored deviceId = "abc123" | attacker sends X-Device-Id: "xyz999"
Result: ALL tokens for this user are immediately revoked → 401 TOKEN_SECURITY_VIOLATION
```

**Defense 2 — Token Rotation Attack Detection**:
Each refresh token can only be used once. If the legitimate user calls `/auth/refresh` first, the old token is deleted. If the attacker then tries to use the same (now-rotated) token, it is no longer found in the database.

```
Attacker uses stolen token after legitimate user already rotated it → 401 TOKEN_REFRESH_ERROR
```

Conversely, if the attacker rotates first, the legitimate user's next refresh attempt will fail. Both paths surface as a detectable anomaly.

**Defense 4 — IP Change Detection**:
The IP address that issued the refresh token is recorded. If the refresh request comes from a different IP, the server rejects it immediately.

```
Legitimate user IP: 203.0.113.10 | Attacker IP: 198.51.100.5
Result: → 401 TOKEN_SECURITY_VIOLATION
```

**Defense 5 — HttpOnly Cookie**:
The refresh token is stored in a `HttpOnly; Secure; SameSite=Strict` cookie, so JavaScript running in the browser (including injected XSS payloads) cannot read it. This eliminates the most common client-side theft vector.

**Combined outcome**: An attacker who steals a refresh token would need to simultaneously spoof the original device fingerprint, originate from the same IP address, and use the token before the legitimate user triggers rotation — an extremely narrow window that also immediately alerts the system.
