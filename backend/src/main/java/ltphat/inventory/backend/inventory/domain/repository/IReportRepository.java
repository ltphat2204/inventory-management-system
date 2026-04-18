package ltphat.inventory.backend.inventory.domain.repository;

import ltphat.inventory.backend.inventory.reporting.inout.dto.InOutStockRawData;

import java.time.ZonedDateTime;
import java.util.List;

public interface IReportRepository {
    List<InOutStockRawData> calculateInOutStock(ZonedDateTime startDate, ZonedDateTime endDate, List<Long> variantIds);
}
