package ltphat.inventory.backend.iam.presentation.controller;

import jakarta.servlet.http.Cookie;
import ltphat.inventory.backend.iam.application.dto.AuthResponse;
import ltphat.inventory.backend.iam.application.dto.AuthResult;
import ltphat.inventory.backend.iam.application.dto.LoginRequest;
import ltphat.inventory.backend.iam.application.dto.UserDto;
import ltphat.inventory.backend.iam.application.service.IAuthService;
import ltphat.inventory.backend.shared.api.exception.GlobalExceptionHandler;
import ltphat.inventory.backend.shared.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

        @MockitoBean
    private IAuthService authService;

        @MockitoBean
        private JwtTokenProvider jwtTokenProvider;

        @MockitoBean
        private UserDetailsService userDetailsService;

    @Test
    void login_shouldReturn200AndSetCookie() throws Exception {
        AuthResult result = AuthResult.builder()
                .response(AuthResponse.builder()
                        .accessToken("access-token")
                        .user(UserDto.builder().id(1L).fullName("Admin").role("ADMIN").build())
                        .build())
                .newRefreshToken("refresh-token")
                .build();

        when(authService.login(any(LoginRequest.class), eq("device-1"), any())).thenReturn(result);

        mockMvc.perform(post("/auth/login")
                        .header("X-Device-Id", "device-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("refreshToken=refresh-token")))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("HttpOnly")))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Path=/api/v1/auth")))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("SameSite=Strict")));
    }

    @Test
    void refresh_shouldReturn401WhenCookieMissing() throws Exception {
        mockMvc.perform(post("/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value("TOKEN_REFRESH_ERROR"));
    }

        @Test
        void refresh_shouldReturn401WhenCookieBlank() throws Exception {
                mockMvc.perform(post("/auth/refresh")
                                                .cookie(new Cookie("refreshToken", "   ")))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error_code").value("TOKEN_REFRESH_ERROR"));
        }

    @Test
    void refresh_shouldReturn200AndRotateCookie() throws Exception {
        AuthResult result = AuthResult.builder()
                .response(AuthResponse.builder()
                        .accessToken("new-access")
                        .user(UserDto.builder().id(1L).fullName("Admin").role("ADMIN").build())
                        .build())
                .newRefreshToken("new-refresh")
                .build();

        when(authService.refreshToken(eq("old-refresh"), eq("device-1"), any())).thenReturn(result);

        mockMvc.perform(post("/auth/refresh")
                        .header("X-Device-Id", "device-1")
                        .cookie(new Cookie("refreshToken", "old-refresh")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new-access"))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("refreshToken=new-refresh")));
    }

    @Test
    void logout_shouldCallServiceWhenCookiePresent() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .cookie(new Cookie("refreshToken", "refresh-value")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")));

        verify(authService).logout("refresh-value");
    }

    @Test
    void logout_shouldNotCallServiceWhenCookieMissing() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(authService, never()).logout(any());
    }

    @Test
    void logout_shouldNotCallServiceWhenCookieBlank() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .cookie(new Cookie("refreshToken", "   ")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(authService, never()).logout(any());
    }
}
