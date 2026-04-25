package ltphat.inventory.backend.iam.presentation.controller;

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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = "cookie.secure=true")
class AuthControllerSecureCookieTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private IAuthService authService;

        @MockitoBean
        private JwtTokenProvider jwtTokenProvider;

        @MockitoBean
        private UserDetailsService userDetailsService;

        @Test
        void login_shouldSetCookieWithSecureWhenCookieSecureTrue() throws Exception {
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
                                .andExpect(header().string("Set-Cookie",
                                                org.hamcrest.Matchers.containsString("Secure")));
        }
}
