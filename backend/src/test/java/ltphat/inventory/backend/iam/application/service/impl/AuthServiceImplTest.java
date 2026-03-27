package ltphat.inventory.backend.iam.application.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import ltphat.inventory.backend.iam.application.AuthApplicationMapper;
import ltphat.inventory.backend.iam.application.dto.AuthResult;
import ltphat.inventory.backend.iam.application.dto.LoginRequest;
import ltphat.inventory.backend.iam.application.dto.UserDto;
import ltphat.inventory.backend.iam.domain.exception.InvalidCredentialsException;
import ltphat.inventory.backend.iam.domain.exception.TokenRefreshException;
import ltphat.inventory.backend.iam.domain.exception.TokenSecurityException;
import ltphat.inventory.backend.iam.domain.model.RefreshToken;
import ltphat.inventory.backend.iam.domain.model.Role;
import ltphat.inventory.backend.iam.domain.model.User;
import ltphat.inventory.backend.iam.domain.repository.IRefreshTokenRepository;
import ltphat.inventory.backend.shared.security.CustomUserDetails;
import ltphat.inventory.backend.shared.security.JwtTokenProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private IRefreshTokenRepository refreshTokenRepository;

    @Mock
    private AuthApplicationMapper mapper;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthServiceImpl authService;

    private User activeUser;
    private User inactiveUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "jwtRefreshExpiration", 604800000L);

        Role role = Role.builder().id(1L).name("ADMIN").build();
        activeUser = User.builder()
                .id(100L)
                .username("admin")
                .passwordHash("encoded")
                .fullName("Admin")
                .role(role)
                .isActive(true)
                .build();

        inactiveUser = User.builder()
                .id(101L)
                .username("inactive")
                .passwordHash("encoded")
                .fullName("Inactive")
                .role(role)
                .isActive(false)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void login_shouldAuthenticateAndReturnTokens_whenCredentialsValid() {
        LoginRequest request = LoginRequest.builder().username("admin").password("password").build();
        CustomUserDetails userDetails = new CustomUserDetails(activeUser);
        UserDto userDto = UserDto.builder().id(activeUser.getId()).fullName("Admin").role("ADMIN").build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtTokenProvider.generateAccessToken(userDetails)).thenReturn("access-token");
        when(mapper.toDto(activeUser)).thenReturn(userDto);
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(httpServletRequest.getRemoteAddr()).thenReturn("10.0.0.1");
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("JUnit");

        AuthResult result = authService.login(request, "device-A", httpServletRequest);

        assertThat(result.getResponse().getAccessToken()).isEqualTo("access-token");
        assertThat(result.getResponse().getUser()).isEqualTo(userDto);
        assertThat(result.getNewRefreshToken()).isNotBlank();

        verify(refreshTokenRepository).deleteByUser(activeUser);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(authentication);
    }

    @Test
    void login_shouldThrowInvalidCredentials_whenAuthenticationFails() {
        LoginRequest request = LoginRequest.builder().username("admin").password("wrong").build();
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("bad credentials"));

        assertThatThrownBy(() -> authService.login(request, "device-A", httpServletRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid username or password");
    }

    @Test
    void login_shouldThrowInvalidCredentials_whenUserInactive() {
        LoginRequest request = LoginRequest.builder().username("inactive").password("password").build();
        CustomUserDetails userDetails = new CustomUserDetails(inactiveUser);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        assertThatThrownBy(() -> authService.login(request, "device-A", httpServletRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Account is disabled");

        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void refreshToken_shouldThrowWhenTokenNotFound() {
        when(refreshTokenRepository.findByToken("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshToken("missing", "device-A", httpServletRequest))
                .isInstanceOf(TokenRefreshException.class)
                .hasMessage("Refresh token is invalid or has already been used");
    }

    @Test
    void refreshToken_shouldDeleteAndThrowWhenExpired() {
        RefreshToken existing = RefreshToken.builder()
                .token("expired-token")
                .user(activeUser)
                .expiryDate(LocalDateTime.now().minusSeconds(1))
                .build();

        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> authService.refreshToken("expired-token", "device-A", httpServletRequest))
                .isInstanceOf(TokenRefreshException.class)
                .hasMessage("Refresh token has expired. Please sign in again");

        verify(refreshTokenRepository).deleteByToken("expired-token");
    }

    @Test
    void refreshToken_shouldRevokeAllAndThrowWhenDeviceMismatch() {
        RefreshToken existing = RefreshToken.builder()
                .token("token")
                .user(activeUser)
                .deviceId("device-A")
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .build();

        when(refreshTokenRepository.findByToken("token")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> authService.refreshToken("token", "device-B", httpServletRequest))
                .isInstanceOf(TokenSecurityException.class)
                .hasMessage("Device mismatch detected. All sessions have been revoked");

        verify(refreshTokenRepository).deleteByUserId(activeUser.getId());
    }

    @Test
    void refreshToken_shouldThrowWhenIpChanges() {
        RefreshToken existing = RefreshToken.builder()
                .token("token")
                .user(activeUser)
                .deviceId("device-A")
                .lastIp("10.0.0.1")
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .build();

        when(refreshTokenRepository.findByToken("token")).thenReturn(Optional.of(existing));
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn("10.0.0.2");

        assertThatThrownBy(() -> authService.refreshToken("token", "device-A", httpServletRequest))
                .isInstanceOf(TokenSecurityException.class)
                .hasMessage("IP change detected. Please re-authenticate");
    }

    @Test
    void refreshToken_shouldRotateTokensAndUseForwardedIp_whenValid() {
        RefreshToken existing = RefreshToken.builder()
                .token("token")
                .user(activeUser)
                .deviceId("device-A")
                .lastIp("10.0.0.1")
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .build();
        UserDto userDto = UserDto.builder().id(activeUser.getId()).fullName("Admin").role("ADMIN").build();

        when(refreshTokenRepository.findByToken("token")).thenReturn(Optional.of(existing));
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 10.0.0.2");
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("JUnit");
        when(jwtTokenProvider.generateAccessToken(any(CustomUserDetails.class))).thenReturn("new-access");
        when(mapper.toDto(activeUser)).thenReturn(userDto);

        AuthResult result = authService.refreshToken("token", "device-A", httpServletRequest);

        assertThat(result.getResponse().getAccessToken()).isEqualTo("new-access");
        assertThat(result.getResponse().getUser()).isEqualTo(userDto);
        assertThat(result.getNewRefreshToken()).isNotBlank();

        verify(refreshTokenRepository).deleteByToken("token");

        ArgumentCaptor<RefreshToken> refreshCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(refreshCaptor.capture());
        assertThat(refreshCaptor.getValue().getLastIp()).isEqualTo("10.0.0.1");
    }

    @Test
    void logout_shouldDeleteTokenAndClearContext() {
        SecurityContextHolder.getContext().setAuthentication(authentication);

        authService.logout("token");

        verify(refreshTokenRepository).deleteByToken("token");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void refreshToken_shouldAllowNullDeviceAndNullLastIp_andUseRemoteAddrWhenForwardedBlank() {
        RefreshToken existing = RefreshToken.builder()
                .token("token")
                .user(activeUser)
                .deviceId(null)
                .lastIp(null)
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .build();
        UserDto userDto = UserDto.builder().id(activeUser.getId()).fullName("Admin").role("ADMIN").build();

        when(refreshTokenRepository.findByToken("token")).thenReturn(Optional.of(existing));
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn("   ");
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("JUnit");
        when(jwtTokenProvider.generateAccessToken(any(CustomUserDetails.class))).thenReturn("new-access");
        when(mapper.toDto(activeUser)).thenReturn(userDto);

        AuthResult result = authService.refreshToken("token", null, httpServletRequest);

        assertThat(result.getResponse().getAccessToken()).isEqualTo("new-access");

        ArgumentCaptor<RefreshToken> refreshCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(refreshCaptor.capture());
        assertThat(refreshCaptor.getValue().getLastIp()).isEqualTo("127.0.0.1");
    }
}
