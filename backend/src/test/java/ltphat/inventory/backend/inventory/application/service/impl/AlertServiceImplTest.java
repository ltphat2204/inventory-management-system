package ltphat.inventory.backend.inventory.application.service.impl;

import ltphat.inventory.backend.inventory.application.dto.AlertDismissRequest;
import ltphat.inventory.backend.inventory.domain.model.AlertType;
import ltphat.inventory.backend.inventory.domain.repository.IInventoryRepository;
import ltphat.inventory.backend.inventory.infrastructure.persistence.repository.SpringDataDismissedAlertRepository;
import ltphat.inventory.backend.iam.domain.model.Role;
import ltphat.inventory.backend.iam.domain.model.User;
import ltphat.inventory.backend.shared.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class AlertServiceImplTest {

    @Mock
    private IInventoryRepository inventoryRepository;

    @Mock
    private SpringDataDismissedAlertRepository dismissedAlertRepository;

    @InjectMocks
    private AlertServiceImpl alertService;

    @BeforeEach
    void setUpSecurityContext() {
        Role role = Role.builder().id(1L).name("MANAGER").build();
        User user = User.builder().id(22L).username("manager").role(role).isActive(true).build();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void dismissAlert_shouldRejectSlowMoving() {
        AlertDismissRequest request = AlertDismissRequest.builder()
                .alertType(AlertType.SLOW_MOVING)
                .variantId(1L)
                .build();

        assertThatThrownBy(() -> alertService.dismissAlert(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not implemented");
    }
}
