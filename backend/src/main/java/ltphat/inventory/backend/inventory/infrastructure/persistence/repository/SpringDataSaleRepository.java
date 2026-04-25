package ltphat.inventory.backend.inventory.infrastructure.persistence.repository;

import ltphat.inventory.backend.inventory.infrastructure.persistence.entity.JpaSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;

@Repository
public interface SpringDataSaleRepository extends JpaRepository<JpaSale, Long> {
    boolean existsByIdempotencyKey(String idempotencyKey);
    boolean existsBySaleNumber(String saleNumber);

    @Query("SELECT COALESCE(SUM(s.totalVnd), 0) FROM JpaSale s WHERE s.saleAt >= :start AND s.saleAt < :end")
    Long sumTotalVndBySaleAtBetween(@Param("start") ZonedDateTime start, @Param("end") ZonedDateTime end);

    long countBySaleAtBetween(ZonedDateTime start, ZonedDateTime end);
}
