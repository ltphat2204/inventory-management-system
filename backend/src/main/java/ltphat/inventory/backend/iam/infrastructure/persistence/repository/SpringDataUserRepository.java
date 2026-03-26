package ltphat.inventory.backend.iam.infrastructure.persistence.repository;

import ltphat.inventory.backend.iam.infrastructure.persistence.entity.JpaUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataUserRepository extends JpaRepository<JpaUser, Long> {
    Optional<JpaUser> findByUsername(String username);
    boolean existsByUsername(String username);
}
