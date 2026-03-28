package ltphat.inventory.backend.shared.api.exception;

import ltphat.inventory.backend.iam.domain.exception.DuplicateUsernameException;
import ltphat.inventory.backend.iam.domain.exception.RoleNotFoundException;
import ltphat.inventory.backend.iam.domain.exception.UserNotFoundException;
import ltphat.inventory.backend.iam.domain.exception.InvalidCredentialsException;
import ltphat.inventory.backend.iam.domain.exception.TokenRefreshException;
import ltphat.inventory.backend.iam.domain.exception.TokenSecurityException;
import ltphat.inventory.backend.catalog.domain.exception.CategoryNotFoundException;
import ltphat.inventory.backend.catalog.domain.exception.CategoryHasProductsException;
import ltphat.inventory.backend.catalog.domain.exception.ProductNotFoundException;
import ltphat.inventory.backend.catalog.domain.exception.VariantNotFoundException;
import ltphat.inventory.backend.catalog.domain.exception.DuplicateProductCodeException;
import ltphat.inventory.backend.catalog.domain.exception.DuplicateVariantSkuException;
import ltphat.inventory.backend.inventory.domain.exception.InventoryNotFoundException;
import ltphat.inventory.backend.shared.api.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_SERVER_ERROR", "An unexpected error occurred: " + ex.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("VALIDATION_ERROR", "Invalid input data", details));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("BAD_REQUEST", ex.getMessage(), null));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("INVALID_CREDENTIALS", ex.getMessage(), null));
    }

    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<ApiResponse<Void>> handleTokenRefreshException(TokenRefreshException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("TOKEN_REFRESH_ERROR", ex.getMessage(), null));
    }

    @ExceptionHandler(TokenSecurityException.class)
    public ResponseEntity<ApiResponse<Void>> handleTokenSecurityException(TokenSecurityException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("TOKEN_SECURITY_VIOLATION", ex.getMessage(), null));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("USER_NOT_FOUND", ex.getMessage(), null));
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleRoleNotFoundException(RoleNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("ROLE_NOT_FOUND", ex.getMessage(), null));
    }

    @ExceptionHandler(DuplicateUsernameException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateUsernameException(DuplicateUsernameException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("DUPLICATE_USERNAME", ex.getMessage(), null));
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleCategoryNotFoundException(CategoryNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("CATEGORY_NOT_FOUND", ex.getMessage(), null));
    }

    @ExceptionHandler(CategoryHasProductsException.class)
    public ResponseEntity<ApiResponse<Void>> handleCategoryHasProductsException(CategoryHasProductsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("CATEGORY_HAS_PRODUCTS", ex.getMessage(), null));
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleProductNotFoundException(ProductNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("PRODUCT_NOT_FOUND", ex.getMessage(), null));
    }

    @ExceptionHandler(VariantNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleVariantNotFoundException(VariantNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("VARIANT_NOT_FOUND", ex.getMessage(), null));
    }

    @ExceptionHandler(DuplicateProductCodeException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateProductCodeException(DuplicateProductCodeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("DUPLICATE_PRODUCT_CODE", ex.getMessage(), null));
    }

    @ExceptionHandler(DuplicateVariantSkuException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateVariantSkuException(DuplicateVariantSkuException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("DUPLICATE_VARIANT_SKU", ex.getMessage(), null));
    }

    @ExceptionHandler(InventoryNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleInventoryNotFoundException(InventoryNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("INVENTORY_NOT_FOUND", ex.getMessage(), null));
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleIdempotencyConflictException(IdempotencyConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("IDEMPOTENCY_CONFLICT", ex.getMessage(), null));
    }
}
