package ltphat.inventory.backend.iam.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "New password must be at least 8 characters")
    private String newPassword;
}
