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
public class Sale {
    private Long id;
    private String saleNumber;
    private ZonedDateTime saleAt;
    private Long cashierId;
    private Long subtotalVnd;
    private Long discountVnd;
    private Long totalVnd;
    private Boolean eInvoiceExported;
    private String notes;
    private String idempotencyKey;
    private List<SaleItem> items;
    private ZonedDateTime createdAt;
}
