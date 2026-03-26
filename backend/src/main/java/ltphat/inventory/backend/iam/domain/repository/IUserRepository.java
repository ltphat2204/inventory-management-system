package ltphat.inventory.backend.iam.domain.repository;

import ltphat.inventory.backend.iam.domain.model.User;
import java.util.Optional;

public interface IUserRepository {
    Optional<User> findByUsername(String username);
    User save(User user);
    Optional<User> findById(Long id);
    boolean existsByUsername(String username);
}
