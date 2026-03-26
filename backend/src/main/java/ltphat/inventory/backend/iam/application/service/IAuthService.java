package ltphat.inventory.backend.iam.application.service;

import jakarta.servlet.http.HttpServletRequest;
import ltphat.inventory.backend.iam.application.dto.AuthResult;
import ltphat.inventory.backend.iam.application.dto.LoginRequest;

public interface IAuthService {
    AuthResult login(LoginRequest request, String deviceId, HttpServletRequest httpRequest);
    AuthResult refreshToken(String rawRefreshToken, String deviceId, HttpServletRequest httpRequest);
    void logout(String rawRefreshToken);
}
