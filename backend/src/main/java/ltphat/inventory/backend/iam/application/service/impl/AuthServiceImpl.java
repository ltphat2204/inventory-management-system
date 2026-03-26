package ltphat.inventory.backend.iam.application.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import ltphat.inventory.backend.iam.application.AuthApplicationMapper;
import ltphat.inventory.backend.iam.application.dto.AuthResponse;
import ltphat.inventory.backend.iam.application.dto.AuthResult;
import ltphat.inventory.backend.iam.application.dto.LoginRequest;
import ltphat.inventory.backend.iam.application.service.IAuthService;
import ltphat.inventory.backend.iam.domain.exception.InvalidCredentialsException;
import ltphat.inventory.backend.iam.domain.exception.TokenRefreshException;
import ltphat.inventory.backend.iam.domain.exception.TokenSecurityException;
import ltphat.inventory.backend.iam.domain.model.RefreshToken;
import ltphat.inventory.backend.iam.domain.model.User;
import ltphat.inventory.backend.iam.domain.repository.IRefreshTokenRepository;
import ltphat.inventory.backend.shared.security.CustomUserDetails;
import ltphat.inventory.backend.shared.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final IRefreshTokenRepository refreshTokenRepository;
    private final AuthApplicationMapper mapper;

    @Value("${jwt.refresh.expiration}")
    private long jwtRefreshExpiration;

    @Override
    @Transactional
    public AuthResult login(LoginRequest request, String deviceId, HttpServletRequest httpRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (Exception e) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        if (!user.getIsActive()) {
            throw new InvalidCredentialsException("Account is disabled");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);

        refreshTokenRepository.deleteByUser(user);

        String rawRefreshToken = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(rawRefreshToken)
                .expiryDate(LocalDateTime.now().plusSeconds(jwtRefreshExpiration / 1000))
                .deviceId(deviceId)
                .lastIp(resolveClientIp(httpRequest))
                .lastUserAgent(httpRequest.getHeader("User-Agent"))
                .lastUsedAt(LocalDateTime.now())
                .build();

        refreshTokenRepository.save(refreshToken);

        AuthResponse response = AuthResponse.builder()
                .accessToken(accessToken)
                .user(mapper.toDto(user))
                .build();

        return AuthResult.builder()
                .response(response)
                .newRefreshToken(rawRefreshToken)
                .build();
    }

    @Override
    @Transactional
    public AuthResult refreshToken(String rawRefreshToken, String deviceId, HttpServletRequest httpRequest) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(rawRefreshToken)
                .orElseThrow(() -> {
                    log.warn("Refresh token not found, possible reuse attack detected");
                    return new TokenRefreshException("Refresh token is invalid or has already been used");
                });

        if (refreshToken.isExpired()) {
            refreshTokenRepository.deleteByToken(refreshToken.getToken());
            throw new TokenRefreshException("Refresh token has expired. Please sign in again");
        }

        if (refreshToken.getDeviceId() != null && !refreshToken.getDeviceId().equals(deviceId)) {
            log.warn("Device mismatch for user {}. Revoking all tokens.", refreshToken.getUser().getId());
            refreshTokenRepository.deleteByUserId(refreshToken.getUser().getId());
            throw new TokenSecurityException("Device mismatch detected. All sessions have been revoked");
        }

        String currentIp = resolveClientIp(httpRequest);
        if (refreshToken.getLastIp() != null && !refreshToken.getLastIp().equals(currentIp)) {
            log.warn("IP change detected for user {}: {} -> {}",
                    refreshToken.getUser().getId(), refreshToken.getLastIp(), currentIp);
            throw new TokenSecurityException("IP change detected. Please re-authenticate");
        }

        refreshTokenRepository.deleteByToken(refreshToken.getToken());

        User user = refreshToken.getUser();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);

        String newRawToken = UUID.randomUUID().toString();
        RefreshToken newRefreshToken = RefreshToken.builder()
                .user(user)
                .token(newRawToken)
                .expiryDate(LocalDateTime.now().plusSeconds(jwtRefreshExpiration / 1000))
                .deviceId(deviceId)
                .lastIp(currentIp)
                .lastUserAgent(httpRequest.getHeader("User-Agent"))
                .lastUsedAt(LocalDateTime.now())
                .build();

        refreshTokenRepository.save(newRefreshToken);

        AuthResponse response = AuthResponse.builder()
                .accessToken(newAccessToken)
                .user(mapper.toDto(user))
                .build();

        return AuthResult.builder()
                .response(response)
                .newRefreshToken(newRawToken)
                .build();
    }

    @Override
    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenRepository.deleteByToken(rawRefreshToken);
        SecurityContextHolder.clearContext();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
