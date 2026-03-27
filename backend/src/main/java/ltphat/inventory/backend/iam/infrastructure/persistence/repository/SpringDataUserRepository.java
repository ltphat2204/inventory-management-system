package ltphat.inventory.backend.iam.infrastructure.persistence.repository;

import ltphat.inventory.backend.iam.infrastructure.persistence.entity.JpaUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataUserRepository extends JpaRepository<JpaUser, Long>, JpaSpecificationExecutor<JpaUser> {
    Optional<JpaUser> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByUsernameAndIdNot(String username, Long id);
}
