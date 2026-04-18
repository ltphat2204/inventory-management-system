package ltphat.inventory.backend.shared.security;

import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigMethodSecurityTest {

    @Test
    void securityConfig_enablesMethodSecurityForPreAuthorize() {
        assertThat(AnnotationUtils.findAnnotation(SecurityConfig.class, EnableMethodSecurity.class))
                .isNotNull();
    }
}
