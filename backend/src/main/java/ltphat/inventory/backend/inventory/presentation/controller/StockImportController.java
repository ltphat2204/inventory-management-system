package ltphat.inventory.backend.inventory.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ltphat.inventory.backend.inventory.application.dto.StockImportRequest;
import ltphat.inventory.backend.inventory.application.dto.StockImportResponse;
import ltphat.inventory.backend.inventory.application.service.StockImportService;
import ltphat.inventory.backend.shared.api.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stock-imports")
@RequiredArgsConstructor
public class StockImportController {

    private final StockImportService stockImportService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<StockImportResponse>> importStock(@Valid @RequestBody StockImportRequest request) {
        StockImportResponse response = stockImportService.importStock(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Stock imported successfully", response));
    }
}
