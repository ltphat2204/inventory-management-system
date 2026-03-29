package ltphat.inventory.backend.inventory.presentation.controller;

import lombok.RequiredArgsConstructor;
import ltphat.inventory.backend.inventory.application.dto.DashboardResponse;
import ltphat.inventory.backend.inventory.application.service.IDashboardService;
import ltphat.inventory.backend.shared.api.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final IDashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @RequestParam(defaultValue = "10") int lowStockLimit) {
        DashboardResponse response = dashboardService.getDashboard(lowStockLimit);
        return ResponseEntity.ok(ApiResponse.success("Success", response));
    }
}
