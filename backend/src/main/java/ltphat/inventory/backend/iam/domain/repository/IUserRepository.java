package ltphat.inventory.backend.iam.domain.repository;

import ltphat.inventory.backend.iam.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface IUserRepository {
    Optional<User> findByUsername(String username);
    User save(User user);
    Optional<User> findById(Long id);
    boolean existsByUsername(String username);
    Page<User> findAll(Pageable pageable, String roleName, Boolean isActive);
    boolean existsByUsernameAndIdNot(String username, Long id);
}
