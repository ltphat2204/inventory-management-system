package ltphat.inventory.backend.inventory.infrastructure.persistence.repository;

import jakarta.persistence.LockModeType;
import ltphat.inventory.backend.inventory.infrastructure.persistence.entity.JpaInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataInventoryRepository extends JpaRepository<JpaInventory, Long> {
    Optional<JpaInventory> findByVariantId(Long variantId);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM JpaInventory i WHERE i.variantId = :variantId")
    Optional<JpaInventory> findByVariantIdWithLock(Long variantId);

    List<JpaInventory> findByVariantIdIn(List<Long> variantIds);
}
