package ltphat.inventory.backend.inventory.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockImportResponse {
    private Long id;
    private String importNumber;
    private String supplierName;
    private String notes;
    private ZonedDateTime importDate;
    private List<StockImportItemResponse> items;
}
