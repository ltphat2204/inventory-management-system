package ltphat.inventory.backend.inventory.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlowMovingItemResponse {
    private Long variantId;
    private String sku;
    private String productName;
    private Integer currentQuantity;
    private ZonedDateTime lastMovementAt;
}
