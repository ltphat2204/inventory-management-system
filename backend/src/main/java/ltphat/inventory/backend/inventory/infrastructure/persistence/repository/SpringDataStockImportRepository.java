package ltphat.inventory.backend.inventory.infrastructure.persistence.repository;

import ltphat.inventory.backend.inventory.infrastructure.persistence.entity.JpaStockImport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataStockImportRepository extends JpaRepository<JpaStockImport, Long> {
    boolean existsByIdempotencyKey(String idempotencyKey);
    boolean existsByImportNumber(String importNumber);
    Optional<JpaStockImport> findByImportNumber(String importNumber);
}
