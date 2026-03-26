package ltphat.inventory.backend.iam.application.service.impl;

import ltphat.inventory.backend.iam.application.AuthApplicationMapper;
import ltphat.inventory.backend.iam.application.dto.AuthResponse;
import ltphat.inventory.backend.iam.application.dto.LoginRequest;
import ltphat.inventory.backend.iam.application.dto.LogoutRequest;
import ltphat.inventory.backend.iam.application.dto.RefreshTokenRequest;
import ltphat.inventory.backend.iam.application.service.IAuthService;
import ltphat.inventory.backend.iam.domain.exception.InvalidCredentialsException;
import ltphat.inventory.backend.iam.domain.exception.TokenRefreshException;
import ltphat.inventory.backend.iam.domain.model.RefreshToken;
import ltphat.inventory.backend.iam.domain.model.User;
import ltphat.inventory.backend.iam.domain.repository.IRefreshTokenRepository;
import ltphat.inventory.backend.shared.security.CustomUserDetails;
import ltphat.inventory.backend.shared.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
    public AuthResponse login(LoginRequest request) {
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
        String refreshTokenString = jwtTokenProvider.generateRefreshToken(userDetails);

        // Invalidate old tokens for this user and save new one
        refreshTokenRepository.deleteByUser(user);
        
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenString)
                .expiryDate(LocalDateTime.now().plusSeconds(jwtRefreshExpiration / 1000))
                .build();
        
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenString)
                .user(mapper.toDto(user))
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        
        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new TokenRefreshException("Refresh token is not in database!"));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.deleteByToken(refreshToken.getToken());
            throw new TokenRefreshException("Refresh token was expired. Please make a new signin request");
        }

        User user = refreshToken.getUser();
        CustomUserDetails userDetails = new CustomUserDetails(user);

        String token = jwtTokenProvider.generateAccessToken(userDetails);
        
        return AuthResponse.builder()
                .accessToken(token)
                .refreshToken(requestRefreshToken)
                .user(mapper.toDto(user))
                .build();
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        refreshTokenRepository.deleteByToken(request.getRefreshToken());
        SecurityContextHolder.clearContext();
    }
}
