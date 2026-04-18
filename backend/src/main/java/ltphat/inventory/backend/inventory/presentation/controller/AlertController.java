package ltphat.inventory.backend.inventory.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ltphat.inventory.backend.inventory.application.dto.AlertDismissRequest;
import ltphat.inventory.backend.inventory.application.dto.AlertDismissResponse;
import ltphat.inventory.backend.inventory.application.service.IAlertService;
import ltphat.inventory.backend.shared.api.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final IAlertService alertService;

    @PostMapping("/dismiss")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<AlertDismissResponse>> dismissAlert(@Valid @RequestBody AlertDismissRequest request) {
        AlertDismissResponse response = alertService.dismissAlert(request);
        return ResponseEntity.ok(ApiResponse.success("Alert dismissed successfully", response));
    }

    @PostMapping("/{id}/dismiss")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<AlertDismissResponse>> dismissLowStockAlert(@PathVariable("id") Long variantId) {
        AlertDismissResponse response = alertService.dismissLowStockAlert(variantId);
        return ResponseEntity.ok(ApiResponse.success("Alert dismissed successfully", response));
    }
}
