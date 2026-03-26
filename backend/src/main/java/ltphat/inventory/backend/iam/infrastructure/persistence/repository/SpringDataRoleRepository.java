package ltphat.inventory.backend.iam.infrastructure.persistence.repository;

import ltphat.inventory.backend.iam.infrastructure.persistence.entity.JpaRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataRoleRepository extends JpaRepository<JpaRole, Long> {
    Optional<JpaRole> findByName(String name);
}
