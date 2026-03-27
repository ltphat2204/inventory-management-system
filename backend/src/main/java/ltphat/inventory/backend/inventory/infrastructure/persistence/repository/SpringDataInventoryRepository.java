package ltphat.inventory.backend.inventory.infrastructure.persistence.repository;

import ltphat.inventory.backend.inventory.infrastructure.persistence.entity.JpaInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataInventoryRepository extends JpaRepository<JpaInventory, Long> {
    Optional<JpaInventory> findByVariantId(Long variantId);
    List<JpaInventory> findByVariantIdIn(List<Long> variantIds);
}
