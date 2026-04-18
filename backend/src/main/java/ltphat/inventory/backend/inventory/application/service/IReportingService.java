package ltphat.inventory.backend.inventory.application.service;

import ltphat.inventory.backend.inventory.reporting.einvoice.dto.EInvoiceDataDTO;
import ltphat.inventory.backend.inventory.reporting.inout.dto.InOutStockReportDTO;

import java.time.ZonedDateTime;
import java.util.List;

public interface IReportingService {
    List<InOutStockReportDTO> generateInOutStockReport(ZonedDateTime startDate, ZonedDateTime endDate, List<Long> variantIds);
    List<EInvoiceDataDTO> generateEInvoiceData(List<Long> saleIds);
}
