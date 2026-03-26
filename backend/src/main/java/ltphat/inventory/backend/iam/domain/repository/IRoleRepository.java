package ltphat.inventory.backend.iam.domain.repository;

import ltphat.inventory.backend.iam.domain.model.Role;
import java.util.Optional;

public interface IRoleRepository {
    Optional<Role> findByName(String name);
    Optional<Role> findById(Long id);
}
