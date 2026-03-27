package ltphat.inventory.backend.catalog.application.service.impl;

import ltphat.inventory.backend.catalog.application.dto.VariantDto;
import ltphat.inventory.backend.catalog.application.dto.VariantResponse;
import ltphat.inventory.backend.catalog.domain.exception.DuplicateVariantSkuException;
import ltphat.inventory.backend.catalog.domain.exception.ProductNotFoundException;
import ltphat.inventory.backend.catalog.domain.exception.VariantNotFoundException;
import ltphat.inventory.backend.catalog.domain.model.Product;
import ltphat.inventory.backend.catalog.domain.model.ProductVariant;
import ltphat.inventory.backend.catalog.domain.repository.ProductRepository;
import ltphat.inventory.backend.catalog.domain.repository.ProductVariantRepository;
import ltphat.inventory.backend.inventory.application.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductVariantServiceImplTest {

    @Mock
    private ProductVariantRepository variantRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private ProductVariantServiceImpl productVariantService;

    private VariantDto variantDto;
    private Product product;

    @BeforeEach
    void setUp() {
        variantDto = new VariantDto();
        variantDto.setSize("S");
        variantDto.setColor("Red");
        variantDto.setDesignStyle("Basic");
        variantDto.setVariantPriceVnd(null);
        variantDto.setBarcode("1234567890");
        variantDto.setLowStockThreshold(10);

        product = Product.builder()
                .id(1L)
                .productCode("PROD001")
                .nameVn("Ao thun")
                .basePriceVnd(100000L)
                .build();
    }

    @Test
    void getVariantsByProductId_shouldThrow_whenProductNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productVariantService.getVariantsByProductId(1L, true))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product not found with id: 1");
    }

    @Test
    void getVariantsByProductId_shouldReturnWithoutStock_whenIncludeStockFalse() {
        ProductVariant variant = ProductVariant.builder()
                .id(11L)
                .productId(1L)
                .sku("PROD001-S-RED-BASIC")
                .size("S")
                .color("Red")
                .designStyle("Basic")
                .isActive(true)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(variantRepository.findByProductId(1L)).thenReturn(List.of(variant));

        List<VariantResponse> responses = productVariantService.getVariantsByProductId(1L, false);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getCurrentQuantity()).isNull();
        verify(inventoryService, never()).getCurrentQuantity(any());
    }

    @Test
    void getVariantsByProductId_shouldReturnWithStockAndLowStockFlag_whenIncludeStockTrue() {
        ProductVariant variant = ProductVariant.builder()
                .id(11L)
                .productId(1L)
                .sku("PROD001-S-RED-BASIC")
                .size("S")
                .color("Red")
                .lowStockThreshold(10)
                .isActive(true)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(variantRepository.findByProductId(1L)).thenReturn(List.of(variant));
        when(inventoryService.getCurrentQuantity(11L)).thenReturn(3);

        List<VariantResponse> responses = productVariantService.getVariantsByProductId(1L, true);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getCurrentQuantity()).isEqualTo(3);
        assertThat(responses.get(0).getLowStock()).isTrue();
    }

    @Test
    void getVariantsByProductId_shouldHandleNullQuantityAndNullThreshold_whenIncludeStockTrue() {
        ProductVariant variant = ProductVariant.builder()
                .id(12L)
                .productId(1L)
                .sku("PROD001-L-GREEN")
                .size("L")
                .color("Green")
                .lowStockThreshold(null)
                .isActive(true)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(variantRepository.findByProductId(1L)).thenReturn(List.of(variant));
        when(inventoryService.getCurrentQuantity(12L)).thenReturn(null);

        List<VariantResponse> responses = productVariantService.getVariantsByProductId(1L, true);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().getCurrentQuantity()).isNull();
        assertThat(responses.getFirst().getLowStock()).isFalse();
    }

    @Test
    void getVariantsByProductId_shouldSetLowStockFalse_whenQuantityMeetsThreshold() {
        ProductVariant variant = ProductVariant.builder()
                .id(13L)
                .productId(1L)
                .sku("PROD001-XL-BLACK")
                .size("XL")
                .color("Black")
                .lowStockThreshold(4)
                .isActive(true)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(variantRepository.findByProductId(1L)).thenReturn(List.of(variant));
        when(inventoryService.getCurrentQuantity(13L)).thenReturn(4);

        List<VariantResponse> responses = productVariantService.getVariantsByProductId(1L, true);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().getLowStock()).isFalse();
    }

    @Test
    void addVariantToProduct_shouldThrow_whenProductNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productVariantService.addVariantToProduct(1L, variantDto))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product not found with id: 1");
    }

    @Test
    void addVariantToProduct_shouldThrow_whenSkuAlreadyExists() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(variantRepository.existsBySku("PROD001-S-RED-BASIC")).thenReturn(true);

        assertThatThrownBy(() -> productVariantService.addVariantToProduct(1L, variantDto))
                .isInstanceOf(DuplicateVariantSkuException.class)
                .hasMessage("Generated SKU already exists: PROD001-S-RED-BASIC");
    }

    @Test
    void addVariantToProduct_shouldUseBasePriceAndInitializeZeroStock_whenPriceIsNull() {
        ProductVariant persisted = ProductVariant.builder()
                .id(22L)
                .productId(1L)
                .sku("PROD001-S-RED-BASIC")
                .size("S")
                .color("Red")
                .variantPriceVnd(100000L)
                .barcode("1234567890")
                .lowStockThreshold(10)
                .isActive(true)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(variantRepository.existsBySku("PROD001-S-RED-BASIC")).thenReturn(false);
        when(variantRepository.save(any(ProductVariant.class))).thenReturn(persisted);

        VariantResponse response = productVariantService.addVariantToProduct(1L, variantDto);

        assertThat(response.getVariantPriceVnd()).isEqualTo(100000L);
        assertThat(response.getCurrentQuantity()).isEqualTo(0);
        verify(inventoryService).initializeZeroStock(22L);
    }

    @Test
    void addVariantToProduct_shouldUseProvidedVariantPrice_whenNotNull() {
        variantDto.setVariantPriceVnd(135000L);
        ProductVariant persisted = ProductVariant.builder()
                .id(23L)
                .productId(1L)
                .sku("PROD001-S-RED-BASIC")
                .variantPriceVnd(135000L)
                .isActive(true)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(variantRepository.existsBySku("PROD001-S-RED-BASIC")).thenReturn(false);
        when(variantRepository.save(any(ProductVariant.class))).thenReturn(persisted);

        VariantResponse response = productVariantService.addVariantToProduct(1L, variantDto);

        assertThat(response.getVariantPriceVnd()).isEqualTo(135000L);
    }

    @Test
    void addVariantToProduct_shouldGenerateSkuWithoutOptionalParts() {
        VariantDto request = new VariantDto();
        request.setSize(" ");
        request.setColor(null);
        request.setDesignStyle(" ");
        request.setVariantPriceVnd(1000L);

        ProductVariant persisted = ProductVariant.builder()
                .id(24L)
                .productId(1L)
                .sku("PROD001")
                .variantPriceVnd(1000L)
                .isActive(true)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(variantRepository.existsBySku("PROD001")).thenReturn(false);
        when(variantRepository.save(any(ProductVariant.class))).thenReturn(persisted);

        VariantResponse response = productVariantService.addVariantToProduct(1L, request);

        assertThat(response.getSku()).isEqualTo("PROD001");
    }

    @Test
    void addVariantToProduct_shouldHandleNullAndBlankOptionalSkuParts() {
        VariantDto request = new VariantDto();
        request.setSize(null);
        request.setColor(" ");
        request.setDesignStyle(null);
        request.setVariantPriceVnd(1100L);

        ProductVariant persisted = ProductVariant.builder()
                .id(25L)
                .productId(1L)
                .sku("PROD001")
                .variantPriceVnd(1100L)
                .isActive(true)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(variantRepository.existsBySku("PROD001")).thenReturn(false);
        when(variantRepository.save(any(ProductVariant.class))).thenReturn(persisted);

        VariantResponse response = productVariantService.addVariantToProduct(1L, request);

        assertThat(response.getSku()).isEqualTo("PROD001");
    }

    @Test
    void getVariant_shouldThrow_whenVariantNotFound() {
        when(variantRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productVariantService.getVariant(1L, 2L))
                .isInstanceOf(VariantNotFoundException.class)
                .hasMessage("Variant not found with id: 2");
    }

    @Test
    void getVariant_shouldThrow_whenVariantBelongsToAnotherProduct() {
        ProductVariant variant = ProductVariant.builder().id(2L).productId(99L).build();
        when(variantRepository.findById(2L)).thenReturn(Optional.of(variant));

        assertThatThrownBy(() -> productVariantService.getVariant(1L, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Variant does not belong to the specified product.");
    }

    @Test
    void getVariant_shouldReturnVariantWithCurrentStock_whenValid() {
        ProductVariant variant = ProductVariant.builder()
                .id(2L)
                .productId(1L)
                .sku("PROD001-S-RED-BASIC")
                .size("S")
                .color("Red")
                .build();
        when(variantRepository.findById(2L)).thenReturn(Optional.of(variant));
        when(inventoryService.getCurrentQuantity(2L)).thenReturn(12);

        VariantResponse response = productVariantService.getVariant(1L, 2L);

        assertThat(response.getSku()).isEqualTo("PROD001-S-RED-BASIC");
        assertThat(response.getCurrentQuantity()).isEqualTo(12);
    }

    @Test
    void updateVariant_shouldThrow_whenVariantNotFound() {
        when(variantRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productVariantService.updateVariant(1L, 2L, variantDto))
                .isInstanceOf(VariantNotFoundException.class)
                .hasMessage("Variant not found with id: 2");
    }

    @Test
    void updateVariant_shouldThrow_whenVariantBelongsToAnotherProduct() {
        ProductVariant existing = ProductVariant.builder().id(2L).productId(99L).build();
        when(variantRepository.findById(2L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> productVariantService.updateVariant(1L, 2L, variantDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Variant does not belong to the specified product.");
    }

    @Test
    void updateVariant_shouldPersistUpdatedFields_whenValid() {
        ProductVariant existing = ProductVariant.builder()
                .id(2L)
                .productId(1L)
                .sku("PROD001-S-RED-BASIC")
                .size("S")
                .color("Red")
                .build();

        VariantDto update = new VariantDto();
        update.setSize("M");
        update.setColor("Blue");
        update.setDesignStyle("Premium");
        update.setVariantPriceVnd(130000L);
        update.setBarcode("B-999");
        update.setLowStockThreshold(6);

        ProductVariant saved = ProductVariant.builder()
                .id(2L)
                .productId(1L)
                .sku("PROD001-S-RED-BASIC")
                .size("M")
                .color("Blue")
                .designStyle("Premium")
                .variantPriceVnd(130000L)
                .barcode("B-999")
                .lowStockThreshold(6)
                .isActive(true)
                .build();

        when(variantRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(variantRepository.save(any(ProductVariant.class))).thenReturn(saved);
        when(inventoryService.getCurrentQuantity(2L)).thenReturn(7);

        VariantResponse response = productVariantService.updateVariant(1L, 2L, update);

        assertThat(response.getSize()).isEqualTo("M");
        assertThat(response.getCurrentQuantity()).isEqualTo(7);
    }

    @Test
    void deleteVariant_shouldThrow_whenStockPositive() {
        ProductVariant existing = ProductVariant.builder().id(2L).productId(1L).isActive(true).build();
        when(variantRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(inventoryService.getCurrentQuantity(2L)).thenReturn(1);

        assertThatThrownBy(() -> productVariantService.deleteVariant(1L, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot delete variant with positive stock.");
    }

    @Test
    void deleteVariant_shouldThrow_whenVariantNotFound() {
        when(variantRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productVariantService.deleteVariant(1L, 2L))
                .isInstanceOf(VariantNotFoundException.class)
                .hasMessage("Variant not found with id: 2");
    }

    @Test
    void deleteVariant_shouldThrow_whenVariantBelongsToAnotherProduct() {
        ProductVariant existing = ProductVariant.builder().id(2L).productId(99L).isActive(true).build();
        when(variantRepository.findById(2L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> productVariantService.deleteVariant(1L, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Variant does not belong to the specified product.");
    }

    @Test
    void deleteVariant_shouldSoftDelete_whenStockIsNullOrZero() {
        ProductVariant existing = ProductVariant.builder().id(2L).productId(1L).isActive(true).build();
        when(variantRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(inventoryService.getCurrentQuantity(2L)).thenReturn(0);

        productVariantService.deleteVariant(1L, 2L);

        ArgumentCaptor<ProductVariant> captor = ArgumentCaptor.forClass(ProductVariant.class);
        verify(variantRepository).save(captor.capture());
        assertThat(captor.getValue().getIsActive()).isFalse();
    }

    @Test
    void deleteVariant_shouldSoftDelete_whenStockIsNull() {
        ProductVariant existing = ProductVariant.builder().id(20L).productId(1L).isActive(true).build();
        when(variantRepository.findById(20L)).thenReturn(Optional.of(existing));
        when(inventoryService.getCurrentQuantity(20L)).thenReturn(null);

        productVariantService.deleteVariant(1L, 20L);

        verify(variantRepository).save(existing);
        assertThat(existing.getIsActive()).isFalse();
    }

    @Test
    void getVariantByBarcode_shouldThrow_whenBarcodeNotFound() {
        when(variantRepository.findByBarcode("404")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productVariantService.getVariantByBarcode("404"))
                .isInstanceOf(VariantNotFoundException.class)
                .hasMessage("No variant found with barcode: 404");
    }

    @Test
    void getVariantByBarcode_shouldReturnQuantityAndProductName() {
        ProductVariant variant = ProductVariant.builder()
                .id(3L)
                .productId(1L)
                .sku("PROD001-L-BLACK")
                .lowStockThreshold(5)
                .barcode("1234567890")
                .build();

        when(variantRepository.findByBarcode("1234567890")).thenReturn(Optional.of(variant));
        when(inventoryService.getCurrentQuantity(3L)).thenReturn(5);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        VariantResponse response = productVariantService.getVariantByBarcode("1234567890");

        assertThat(response.getProductNameVn()).isEqualTo("Ao thun");
        assertThat(response.getCurrentQuantity()).isEqualTo(5);
        assertThat(response.getLowStock()).isFalse();
    }

    @Test
    void getVariantByBarcode_shouldMarkLowStockTrue() {
        ProductVariant variant = ProductVariant.builder()
                .id(30L)
                .productId(1L)
                .sku("PROD001-S-RED")
                .lowStockThreshold(5)
                .barcode("LOW")
                .build();

        when(variantRepository.findByBarcode("LOW")).thenReturn(Optional.of(variant));
        when(inventoryService.getCurrentQuantity(30L)).thenReturn(2);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        VariantResponse response = productVariantService.getVariantByBarcode("LOW");

        assertThat(response.getLowStock()).isTrue();
    }

    @Test
    void getVariantByBarcode_shouldHandleNullQuantityAndThresholdDefault() {
        ProductVariant variant = ProductVariant.builder()
                .id(4L)
                .productId(1L)
                .sku("PROD001-XL-GREEN")
                .lowStockThreshold(null)
                .barcode("NQ")
                .build();

        when(variantRepository.findByBarcode("NQ")).thenReturn(Optional.of(variant));
        when(inventoryService.getCurrentQuantity(4L)).thenReturn(null);
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        VariantResponse response = productVariantService.getVariantByBarcode("NQ");

        assertThat(response.getCurrentQuantity()).isNull();
        assertThat(response.getLowStock()).isFalse();
        assertThat(response.getProductNameVn()).isNull();
    }
}