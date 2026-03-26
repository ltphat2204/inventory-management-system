package ltphat.inventory.backend.iam.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ltphat.inventory.backend.iam.application.dto.*;
import ltphat.inventory.backend.iam.application.service.IUserManagementAppService;
import ltphat.inventory.backend.shared.api.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserManagementAppService userManagementAppService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserDetailDto>>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean isActive) {
        
        Page<UserDetailDto> users = userManagementAppService.listUsers(page, limit, sort, role, isActive);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved", users));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDetailDto>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserDetailDto user = userManagementAppService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", user));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDetailDto>> getUser(@PathVariable Long id) {
        UserDetailDto user = userManagementAppService.getUser(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved", user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDetailDto>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserDetailDto user = userManagementAppService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable Long id) {
        userManagementAppService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deactivated successfully", null));
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetPasswordRequest request) {
        userManagementAppService.resetPassword(id, request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", null));
    }
}
