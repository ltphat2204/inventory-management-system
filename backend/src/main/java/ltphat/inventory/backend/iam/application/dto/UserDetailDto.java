package ltphat.inventory.backend.iam.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserDetailDto {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private RoleDto role;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
