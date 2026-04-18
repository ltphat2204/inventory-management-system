package ltphat.inventory.backend.inventory.infrastructure.persistence.repository;

import ltphat.inventory.backend.inventory.infrastructure.persistence.entity.JpaInventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface SpringDataInventoryTransactionRepository extends JpaRepository<JpaInventoryTransaction, Long>, JpaSpecificationExecutor<JpaInventoryTransaction> {
	boolean existsByIdempotencyKey(String idempotencyKey);

	@org.springframework.data.jpa.repository.Query(value = """
			SELECT variant_id as variantId,
			       SUM(CASE WHEN performed_at < :startDate THEN quantity_change ELSE 0 END)  AS beginningQty,
			       SUM(CASE WHEN movement_type = 'IMPORT' AND performed_at BETWEEN :startDate AND :endDate
			                THEN quantity_change ELSE 0 END)                                 AS importQty,
			       SUM(CASE WHEN movement_type IN ('SALE','ADJUSTMENT') AND quantity_change < 0
			                AND performed_at BETWEEN :startDate AND :endDate
			                THEN ABS(quantity_change) ELSE 0 END)                            AS exportQty,
			       SUM(CASE WHEN performed_at <= :endDate THEN quantity_change ELSE 0 END)   AS endingQty
			FROM inventory_transactions
			WHERE variant_id IN (:variantIds)
			  AND performed_at <= :endDate
			GROUP BY variant_id
			""", nativeQuery = true)
	java.util.List<ltphat.inventory.backend.inventory.reporting.inout.dto.InOutStockRawData> calculateInOutStock(
			@org.springframework.data.repository.query.Param("startDate") java.time.ZonedDateTime startDate,
			@org.springframework.data.repository.query.Param("endDate") java.time.ZonedDateTime endDate,
			@org.springframework.data.repository.query.Param("variantIds") java.util.List<Long> variantIds
	);
}
