package ltphat.inventory.backend.catalog.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryRequest {
    
    @NotBlank(message = "Vietnamese name must not be blank")
    private String nameVn;
    
    private String nameEn;
    private String description;
}
