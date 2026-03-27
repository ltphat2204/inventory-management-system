package ltphat.inventory.backend.catalog.infrastructure.persistence.repository;

import ltphat.inventory.backend.catalog.infrastructure.persistence.entity.JpaCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataCategoryRepository extends JpaRepository<JpaCategory, Long> {
}
