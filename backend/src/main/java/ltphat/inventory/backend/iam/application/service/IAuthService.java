package ltphat.inventory.backend.iam.application.service;

import ltphat.inventory.backend.iam.application.dto.AuthResponse;
import ltphat.inventory.backend.iam.application.dto.LoginRequest;
import ltphat.inventory.backend.iam.application.dto.LogoutRequest;
import ltphat.inventory.backend.iam.application.dto.RefreshTokenRequest;

public interface IAuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void logout(LogoutRequest request);
}
