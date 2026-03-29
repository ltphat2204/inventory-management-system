package ltphat.inventory.backend.inventory.presentation.controller;

import ltphat.inventory.backend.inventory.application.dto.DashboardResponse;
import ltphat.inventory.backend.inventory.application.service.IDashboardService;
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
class DashboardControllerTest {

    @Mock
    private IDashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    @Test
    void getDashboard_shouldReturn200() {
        DashboardResponse response = DashboardResponse.builder()
                .totalSkuCount(100)
                .totalStockValueVnd(50_000_000L)
                .lowStockCount(5)
                .todaysSalesTotalVnd(2_000_000L)
                .build();

        when(dashboardService.getDashboard(10)).thenReturn(response);

        ResponseEntity<ApiResponse<DashboardResponse>> result = dashboardController.getDashboard(10);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().isSuccess()).isTrue();
        assertThat(result.getBody().getData().getLowStockCount()).isEqualTo(5);
    }
}
