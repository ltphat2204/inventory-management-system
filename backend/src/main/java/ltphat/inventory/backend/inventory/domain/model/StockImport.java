package ltphat.inventory.backend.inventory.domain.model;

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
public class StockImport {
    private Long id;
    private String importNumber;
    private String supplierName;
    private Long userId;
    private ZonedDateTime importDate;
    private String notes;
    private String idempotencyKey;
    private List<StockImportItem> items;
    private ZonedDateTime createdAt;
}
