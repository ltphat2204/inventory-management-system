package ltphat.inventory.backend.inventory.presentation.controller;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import ltphat.inventory.backend.inventory.application.service.IReportingService;
import ltphat.inventory.backend.inventory.reporting.einvoice.dto.EInvoiceDataDTO;
import ltphat.inventory.backend.inventory.reporting.inout.dto.InOutStockReportDTO;
import ltphat.inventory.backend.shared.util.ExportFileUtil;
import ltphat.inventory.backend.shared.api.response.ApiResponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final IReportingService reportingService;

    @GetMapping("/in-out-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    public ResponseEntity<?> getInOutStockReport(
            @RequestParam("dateFrom") ZonedDateTime dateFrom,
            @RequestParam("dateTo") ZonedDateTime dateTo,
            @RequestParam(value = "variantIds", required = false) List<Long> variantIds,
            @RequestParam(value = "format", defaultValue = "JSON") String format
    ) {
        List<InOutStockReportDTO> data = reportingService.generateInOutStockReport(dateFrom, dateTo, variantIds);

        if ("EXCEL".equalsIgnoreCase(format)) {
            byte[] excelFile = ExportFileUtil.generateInOutStockExcel(data);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=in_out_stock.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new ByteArrayResource(excelFile));
        } else if ("PDF".equalsIgnoreCase(format)) {
            byte[] pdfFile = ExportFileUtil.generateInOutStockPdf(data);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=in_out_stock.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new ByteArrayResource(pdfFile));
        }

        return ResponseEntity.ok(ApiResponse.success("In-Out-Stock report generated", data));
    }

    @GetMapping("/e-invoice-data")
    @PreAuthorize("hasAnyRole('ADMIN', 'VIEWER')")
    public ResponseEntity<?> getEInvoiceData(
            @RequestParam("saleIds") List<Long> saleIds,
            @RequestParam(value = "format", defaultValue = "JSON") String format
    ) throws Exception {
        List<EInvoiceDataDTO> data = reportingService.generateEInvoiceData(saleIds);

        if ("XML".equalsIgnoreCase(format)) {
            XmlMapper xmlMapper = new XmlMapper();
            xmlMapper.registerModule(new JavaTimeModule());
            String xml = xmlMapper.writeValueAsString(data);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=e_invoice.xml")
                    .contentType(MediaType.APPLICATION_XML)
                    .body(xml);
        } else if ("PDF".equalsIgnoreCase(format)) {
            byte[] pdfFile = ExportFileUtil.generateEInvoicePdf(data);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=e_invoice.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new ByteArrayResource(pdfFile));
        }

        return ResponseEntity.ok(ApiResponse.success("E-Invoice data generated", data));
    }
}
