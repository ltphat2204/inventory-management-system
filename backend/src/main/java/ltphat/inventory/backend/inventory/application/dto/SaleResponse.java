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
public class SaleResponse {
    private Long id;
    private String saleNumber;
    private ZonedDateTime saleAt;
    private Long subtotalVnd;
    private Long discountVnd;
    private Long totalVnd;
    private Boolean eInvoiceExported;
    private String notes;
    private List<SaleItemResponse> items;
}
