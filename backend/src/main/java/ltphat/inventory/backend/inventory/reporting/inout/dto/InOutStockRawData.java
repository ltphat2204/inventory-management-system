package ltphat.inventory.backend.inventory.reporting.inout.dto;

public interface InOutStockRawData {
    Long getVariantId();
    Integer getBeginningQty();
    Integer getImportQty();
    Integer getExportQty();
    Integer getEndingQty();
}
