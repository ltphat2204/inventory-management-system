package ltphat.inventory.backend.inventory.presentation.controller;

import ltphat.inventory.backend.inventory.application.dto.InventoryAdjustmentRequest;
import ltphat.inventory.backend.inventory.application.dto.InventoryAdjustmentResponse;
import ltphat.inventory.backend.inventory.application.service.IInventoryAdjustmentService;
import ltphat.inventory.backend.shared.api.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryAdjustmentControllerTest {

    @Mock
    private IInventoryAdjustmentService inventoryAdjustmentService;

    @InjectMocks
    private InventoryAdjustmentController inventoryAdjustmentController;

    @Test
    void adjustStock_shouldReturn201() {
        InventoryAdjustmentRequest request = InventoryAdjustmentRequest.builder()
                .idempotencyKey("adj-1")
                .build();

        InventoryAdjustmentResponse response = InventoryAdjustmentResponse.builder()
                .idempotencyKey("adj-1")
                .build();

        when(inventoryAdjustmentService.adjustStock(request)).thenReturn(response);

        ResponseEntity<ApiResponse<InventoryAdjustmentResponse>> result = inventoryAdjustmentController.adjustStock(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().isSuccess()).isTrue();
        assertThat(result.getBody().getData().getIdempotencyKey()).isEqualTo("adj-1");
    }
}
