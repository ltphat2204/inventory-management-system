package ltphat.inventory.backend.catalog.infrastructure.persistence.repository;

import ltphat.inventory.backend.catalog.infrastructure.persistence.entity.JpaProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataProductRepository extends JpaRepository<JpaProduct, Long>, JpaSpecificationExecutor<JpaProduct> {
    Optional<JpaProduct> findByProductCode(String productCode);
    boolean existsByProductCode(String productCode);
}
