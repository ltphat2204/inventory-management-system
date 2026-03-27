package ltphat.inventory.backend.iam.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoleDto {
    private Long id;
    private String name;
}
