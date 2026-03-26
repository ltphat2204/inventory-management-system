package ltphat.inventory.backend.iam.presentation.controller;

import jakarta.validation.Valid;
import ltphat.inventory.backend.iam.application.dto.AuthResponse;
import ltphat.inventory.backend.iam.application.dto.LoginRequest;
import ltphat.inventory.backend.iam.application.dto.LogoutRequest;
import ltphat.inventory.backend.iam.application.dto.RefreshTokenRequest;
import ltphat.inventory.backend.iam.application.service.IAuthService;
import ltphat.inventory.backend.shared.api.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }
}
