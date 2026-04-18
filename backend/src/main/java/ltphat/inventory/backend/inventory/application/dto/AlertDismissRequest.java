package ltphat.inventory.backend.inventory.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ltphat.inventory.backend.inventory.domain.model.AlertType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertDismissRequest {

    @NotNull(message = "Alert type is required")
    private AlertType alertType;

    @NotNull(message = "Variant ID is required")
    private Long variantId;
}
