package ltphat.inventory.backend.iam.domain.command;

import lombok.Builder;
import lombok.Getter;
import ltphat.inventory.backend.iam.domain.model.Role;

@Getter
@Builder
public class UpdateUserCommand {
    private String fullName;
    private String email;
    private Role role;
    private String passwordHash; // Optional
}
