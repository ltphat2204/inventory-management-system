package ltphat.inventory.backend.inventory.infrastructure.persistence.repository;

import ltphat.inventory.backend.inventory.domain.model.AlertType;
import ltphat.inventory.backend.inventory.infrastructure.persistence.entity.JpaDismissedAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataDismissedAlertRepository extends JpaRepository<JpaDismissedAlert, Long> {
    Optional<JpaDismissedAlert> findByUserIdAndVariantIdAndAlertType(Long userId, Long variantId, AlertType alertType);
}
