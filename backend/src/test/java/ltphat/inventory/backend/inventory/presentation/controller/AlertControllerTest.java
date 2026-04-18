package ltphat.inventory.backend.inventory.presentation.controller;

import ltphat.inventory.backend.inventory.application.dto.AlertDismissRequest;
import ltphat.inventory.backend.inventory.application.dto.AlertDismissResponse;
import ltphat.inventory.backend.inventory.application.service.IAlertService;
import ltphat.inventory.backend.inventory.domain.model.AlertType;
import ltphat.inventory.backend.shared.api.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertControllerTest {

    @Mock
    private IAlertService alertService;

    @InjectMocks
    private AlertController alertController;

    @Test
    void dismissAlert_shouldReturn200_forLowStockBody() {
        AlertDismissResponse response = AlertDismissResponse.builder()
                .variantId(101L)
                .alertType("LOW_STOCK")
                .dismissedAt(ZonedDateTime.now())
                .build();

        AlertDismissRequest request = AlertDismissRequest.builder()
                .alertType(AlertType.LOW_STOCK)
                .variantId(101L)
                .build();

        when(alertService.dismissAlert(request)).thenReturn(response);

        ResponseEntity<ApiResponse<AlertDismissResponse>> result = alertController.dismissAlert(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().isSuccess()).isTrue();
        assertThat(result.getBody().getData().getVariantId()).isEqualTo(101L);
    }

    @Test
    void dismissLowStockAlert_shouldReturn200() {
        AlertDismissResponse response = AlertDismissResponse.builder()
                .variantId(101L)
                .alertType("LOW_STOCK")
                .dismissedAt(ZonedDateTime.now())
                .build();

        when(alertService.dismissLowStockAlert(101L)).thenReturn(response);

        ResponseEntity<ApiResponse<AlertDismissResponse>> result = alertController.dismissLowStockAlert(101L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().isSuccess()).isTrue();
        assertThat(result.getBody().getData().getVariantId()).isEqualTo(101L);
    }
}
