package ltphat.inventory.backend.inventory.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import ltphat.inventory.backend.inventory.domain.repository.IReportRepository;
import ltphat.inventory.backend.inventory.reporting.inout.dto.InOutStockRawData;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReportRepositoryAdapter implements IReportRepository {

    private final SpringDataInventoryTransactionRepository transactionRepository;

    @Override
    public List<InOutStockRawData> calculateInOutStock(ZonedDateTime startDate, ZonedDateTime endDate, List<Long> variantIds) {
        return transactionRepository.calculateInOutStock(startDate, endDate, variantIds);
    }
}
