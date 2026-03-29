package ltphat.inventory.backend.inventory.presentation.controller;

import ltphat.inventory.backend.inventory.application.dto.SaleItemRequest;
import ltphat.inventory.backend.inventory.application.dto.SaleRequest;
import ltphat.inventory.backend.inventory.application.dto.SaleResponse;
import ltphat.inventory.backend.inventory.application.service.ISaleService;
import ltphat.inventory.backend.shared.api.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaleControllerTest {

    @Mock
    private ISaleService saleService;

    @InjectMocks
    private SaleController saleController;

    @Test
    void createSale_shouldReturn201() {
        SaleRequest request = SaleRequest.builder()
                .idempotencyKey("idem-1")
                .items(List.of(SaleItemRequest.builder().variantId(101L).quantity(1).build()))
                .build();

        SaleResponse response = SaleResponse.builder()
                .id(1L)
                .saleNumber("S-20260329123000-1234")
                .subtotalVnd(100_000L)
                .discountVnd(0L)
                .totalVnd(100_000L)
                .build();

        when(saleService.createSale(request)).thenReturn(response);

        ResponseEntity<ApiResponse<SaleResponse>> result = saleController.createSale(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().isSuccess()).isTrue();
        assertThat(result.getBody().getData().getId()).isEqualTo(1L);
    }
}
