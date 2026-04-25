package ltphat.inventory.backend.inventory.infrastructure.persistence.repository;

import jakarta.persistence.LockModeType;
import ltphat.inventory.backend.inventory.infrastructure.persistence.entity.JpaInventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

        @Query("""
                        SELECT
                                v.id as variantId,
                                v.sku as variantSku,
                                p.id as productId,
                                COALESCE(p.nameVn, p.nameEn) as productName,
                                i.currentQuantity as currentQuantity,
                                v.lowStockThreshold as lowStockThreshold
                        FROM JpaInventory i
                        JOIN ltphat.inventory.backend.catalog.infrastructure.persistence.entity.JpaProductVariant v ON v.id = i.variantId
                        JOIN v.product p
                        WHERE (:productId IS NULL OR p.id = :productId)
                                                        AND (:lowStockOnly IS NULL OR :lowStockOnly = FALSE OR i.currentQuantity <= COALESCE(v.lowStockThreshold, 0))
                        """)
        Page<InventoryOverviewProjection> findInventoryOverview(
                        @Param("lowStockOnly") Boolean lowStockOnly,
                        @Param("productId") Long productId,
                        Pageable pageable);

        @Query("""
                        SELECT CASE WHEN COUNT(i) > 0 THEN TRUE ELSE FALSE END
                        FROM JpaInventory i
                        JOIN ltphat.inventory.backend.catalog.infrastructure.persistence.entity.JpaProductVariant v ON v.id = i.variantId
                        WHERE i.variantId = :variantId
                            AND i.currentQuantity <= COALESCE(v.lowStockThreshold, 0)
                        """)
        boolean isVariantLowStock(@Param("variantId") Long variantId);

        @Query("""
                        SELECT COUNT(i)
                        FROM JpaInventory i
                        """)
        long countTotalSkus();

        @Query("""
                        SELECT COALESCE(SUM(i.totalValueVnd), 0)
                        FROM JpaInventory i
                        """)
        long sumTotalStockValueVnd();

        @Query("""
                        SELECT COUNT(i)
                        FROM JpaInventory i
                        JOIN ltphat.inventory.backend.catalog.infrastructure.persistence.entity.JpaProductVariant v ON v.id = i.variantId
                        WHERE i.currentQuantity <= COALESCE(v.lowStockThreshold, 0)
                            AND NOT EXISTS (
                                SELECT 1
                                FROM JpaDismissedAlert da
                                WHERE da.userId = :userId
                                    AND da.variantId = i.variantId
                                    AND da.alertType = ltphat.inventory.backend.inventory.domain.model.AlertType.LOW_STOCK
                            )
                        """)
        long countActiveLowStock(@Param("userId") Long userId);

        @Query("""
                        SELECT
                                v.id as variantId,
                                v.sku as variantSku,
                                p.id as productId,
                                COALESCE(p.nameVn, p.nameEn) as productName,
                                i.currentQuantity as currentQuantity,
                                v.lowStockThreshold as lowStockThreshold
                        FROM JpaInventory i
                        JOIN ltphat.inventory.backend.catalog.infrastructure.persistence.entity.JpaProductVariant v ON v.id = i.variantId
                        JOIN v.product p
                        WHERE i.currentQuantity <= COALESCE(v.lowStockThreshold, 0)
                            AND NOT EXISTS (
                                SELECT 1
                                FROM JpaDismissedAlert da
                                WHERE da.userId = :userId
                                    AND da.variantId = i.variantId
                                    AND da.alertType = ltphat.inventory.backend.inventory.domain.model.AlertType.LOW_STOCK
                            )
                        """)
        Page<InventoryOverviewProjection> findActiveLowStockOverview(@Param("userId") Long userId, Pageable pageable);

        @Query("""
                        SELECT
                                v.id as variantId,
                                v.sku as variantSku,
                                COALESCE(p.nameVn, p.nameEn) as productName,
                                i.currentQuantity as currentQuantity,
                                MAX(t.performedAt) as lastMovementAt
                        FROM JpaInventory i
                        JOIN ltphat.inventory.backend.catalog.infrastructure.persistence.entity.JpaProductVariant v ON v.id = i.variantId
                        JOIN v.product p
                        LEFT JOIN JpaInventoryTransaction t ON t.variantId = v.id
                        GROUP BY v.id, v.sku, p.nameVn, p.nameEn, i.currentQuantity
                        HAVING MAX(t.performedAt) IS NULL OR MAX(t.performedAt) < :thresholdDate
                        """)
        List<SlowMovingProjection> findSlowMovingProducts(@Param("thresholdDate") java.time.ZonedDateTime thresholdDate, Pageable pageable);
}
