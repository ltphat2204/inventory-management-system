package ltphat.inventory.backend.inventory.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {
    private Long id;
    private Long variantId;
    private Integer quantity;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
