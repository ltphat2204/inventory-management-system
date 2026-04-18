package ltphat.inventory.backend.shared.util;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import ltphat.inventory.backend.inventory.reporting.einvoice.dto.EInvoiceDataDTO;
import ltphat.inventory.backend.inventory.reporting.inout.dto.InOutStockReportDTO;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class ExportFileUtil {

    public static byte[] generateInOutStockExcel(List<InOutStockReportDTO> data) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Nhập Xuất Tồn");
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("SKU");
            headerRow.createCell(1).setCellValue("Tên Sản Phẩm");
            headerRow.createCell(2).setCellValue("Tồn Đầu Kỳ");
            headerRow.createCell(3).setCellValue("Nhập Trong Kỳ");
            headerRow.createCell(4).setCellValue("Xuất Trong Kỳ");
            headerRow.createCell(5).setCellValue("Tồn Cuối Kỳ");

            int rowIdx = 1;
            for (InOutStockReportDTO item : data) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(item.getSku());
                row.createCell(1).setCellValue(item.getProductNameVn());
                row.createCell(2).setCellValue(item.getBeginningQty());
                row.createCell(3).setCellValue(item.getImportQty());
                row.createCell(4).setCellValue(item.getExportQty());
                row.createCell(5).setCellValue(item.getEndingQty());
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating Excel report", e);
        }
    }

    public static byte[] generateInOutStockPdf(List<InOutStockReportDTO> data) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("Bao Cao Nhap Xuat Ton"));
            for (InOutStockReportDTO item : data) {
                document.add(new Paragraph(String.format("%s - %s: BD:%d, IN:%d, OUT:%d, END:%d",
                        item.getSku(), item.getProductNameVn(), item.getBeginningQty(), item.getImportQty(), item.getExportQty(), item.getEndingQty())));
            }
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF report", e);
        }
    }

    public static byte[] generateEInvoicePdf(List<EInvoiceDataDTO> data) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("Hoa Don Dien Tu (E-Invoice Data Export)"));
            for (EInvoiceDataDTO item : data) {
                document.add(new Paragraph(String.format("Sale Number: %s - Total: %d", item.getSaleNumber(), item.getTotalVnd())));
            }
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating E-Invoice PDF", e);
        }
    }
}
