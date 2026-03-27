package ltphat.inventory.backend.catalog.infrastructure.persistence.repository;

import ltphat.inventory.backend.catalog.infrastructure.persistence.entity.JpaProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataProductVariantRepository extends JpaRepository<JpaProductVariant, Long> {
    Optional<JpaProductVariant> findBySku(String sku);
    Optional<JpaProductVariant> findByBarcode(String barcode);
    boolean existsBySku(String sku);
    List<JpaProductVariant> findByProductId(Long productId);
}
