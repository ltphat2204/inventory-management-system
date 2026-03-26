package ltphat.inventory.backend.iam.presentation.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import ltphat.inventory.backend.iam.application.dto.AuthResponse;
import ltphat.inventory.backend.iam.application.dto.AuthResult;
import ltphat.inventory.backend.iam.application.dto.LoginRequest;
import ltphat.inventory.backend.iam.application.service.IAuthService;
import ltphat.inventory.backend.iam.domain.exception.TokenRefreshException;
import ltphat.inventory.backend.shared.api.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    private static final int COOKIE_MAX_AGE_SECONDS = 7 * 24 * 60 * 60; // 7 days

    private final IAuthService authService;

    @Value("${cookie.secure:true}")
    private boolean cookieSecure;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            @RequestHeader(value = "X-Device-Id", defaultValue = "") String deviceId,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        AuthResult result = authService.login(request, deviceId, httpRequest);
        setRefreshTokenCookie(httpResponse, result.getNewRefreshToken());

        return ResponseEntity.ok(ApiResponse.success("Login successful", result.getResponse()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            @RequestHeader(value = "X-Device-Id", defaultValue = "") String deviceId,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new TokenRefreshException("Missing refresh token cookie");
        }

        AuthResult result = authService.refreshToken(refreshToken, deviceId, httpRequest);
        setRefreshTokenCookie(httpResponse, result.getNewRefreshToken());

        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", result.getResponse()));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletResponse httpResponse) {

        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.logout(refreshToken);
        }

        clearRefreshTokenCookie(httpResponse);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String tokenValue) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, tokenValue);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge(COOKIE_MAX_AGE_SECONDS);
        response.setHeader("Set-Cookie", buildSetCookieHeader(tokenValue, COOKIE_MAX_AGE_SECONDS));
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        response.setHeader("Set-Cookie", buildSetCookieHeader("", 0));
    }

    private String buildSetCookieHeader(String value, int maxAge) {
        String secure = cookieSecure ? "; Secure" : "";
        return REFRESH_TOKEN_COOKIE + "=" + value
                + "; Path=/api/v1/auth"
                + "; Max-Age=" + maxAge
                + "; HttpOnly"
                + secure
                + "; SameSite=Strict";
    }
}
