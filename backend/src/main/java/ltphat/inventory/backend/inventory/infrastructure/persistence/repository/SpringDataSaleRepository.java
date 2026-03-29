package ltphat.inventory.backend.inventory.infrastructure.persistence.repository;

import ltphat.inventory.backend.inventory.infrastructure.persistence.entity.JpaSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataSaleRepository extends JpaRepository<JpaSale, Long> {
    boolean existsByIdempotencyKey(String idempotencyKey);
    boolean existsBySaleNumber(String saleNumber);
}
