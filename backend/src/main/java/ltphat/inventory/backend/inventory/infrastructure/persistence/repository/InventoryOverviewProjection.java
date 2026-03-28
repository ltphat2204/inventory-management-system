package ltphat.inventory.backend.inventory.infrastructure.persistence.repository;

public interface InventoryOverviewProjection {
    Long getVariantId();
    String getVariantSku();
    Long getProductId();
    String getProductName();
    Integer getCurrentQuantity();
    Integer getLowStockThreshold();
}
