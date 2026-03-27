package ltphat.inventory.backend.catalog.domain.repository;

import ltphat.inventory.backend.catalog.domain.model.ProductVariant;
import java.util.Optional;
import java.util.List;

public interface ProductVariantRepository {
    ProductVariant save(ProductVariant variant);
    List<ProductVariant> saveAll(List<ProductVariant> variants);
    Optional<ProductVariant> findById(Long id);
    Optional<ProductVariant> findBySku(String sku);
    Optional<ProductVariant> findByBarcode(String barcode);
    boolean existsBySku(String sku);
    List<ProductVariant> findByProductId(Long productId);
    void deleteById(Long id);
}
