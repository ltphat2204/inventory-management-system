package ltphat.inventory.backend.catalog.application.service.impl;

import ltphat.inventory.backend.catalog.application.dto.CreateProductRequest;
import ltphat.inventory.backend.catalog.application.dto.ProductResponse;
import ltphat.inventory.backend.catalog.application.dto.VariantDto;
import ltphat.inventory.backend.catalog.domain.exception.DuplicateProductCodeException;
import ltphat.inventory.backend.catalog.domain.exception.DuplicateVariantSkuException;
import ltphat.inventory.backend.catalog.domain.exception.ProductNotFoundException;
import ltphat.inventory.backend.catalog.domain.model.Product;
import ltphat.inventory.backend.catalog.domain.model.ProductVariant;
import ltphat.inventory.backend.catalog.domain.repository.ProductRepository;
import ltphat.inventory.backend.catalog.domain.repository.ProductVariantRepository;
import ltphat.inventory.backend.catalog.infrastructure.persistence.entity.JpaProduct;
import ltphat.inventory.backend.catalog.infrastructure.persistence.entity.JpaProductVariant;
import ltphat.inventory.backend.catalog.infrastructure.persistence.repository.SpringDataProductRepository;
import ltphat.inventory.backend.inventory.application.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariantRepository variantRepository;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private SpringDataProductRepository springDataProductRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private CreateProductRequest createRequest;

    @BeforeEach
    void setUp() {
        createRequest = new CreateProductRequest();
        createRequest.setProductCode("PROD001");
        createRequest.setNameVn("Ao thun");
        createRequest.setNameEn("T-shirt");
        createRequest.setCategoryId(10L);
        createRequest.setBasePriceVnd(120000L);
        createRequest.setVatRate(new BigDecimal("10.00"));
        createRequest.setDescription("Cotton");

        VariantDto first = new VariantDto();
        first.setSize("S");
        first.setColor("Red");
        first.setDesignStyle("Basic");
        first.setVariantPriceVnd(null);
        first.setBarcode("111");
        first.setLowStockThreshold(5);

        VariantDto second = new VariantDto();
        second.setSize("M");
        second.setColor("Blue");
        second.setDesignStyle("");
        second.setVariantPriceVnd(150000L);
        second.setBarcode("222");
        second.setLowStockThreshold(8);

        createRequest.setVariants(List.of(first, second));
    }

    @Test
    void createProduct_shouldThrow_whenProductCodeAlreadyExists() {
        when(productRepository.existsByProductCode("PROD001")).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(createRequest, 99L))
                .isInstanceOf(DuplicateProductCodeException.class)
                .hasMessage("Product code already exists: PROD001");

        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_shouldThrow_whenDuplicateVariantCombinationInRequest() {
        VariantDto duplicate = new VariantDto();
        duplicate.setSize("S");
        duplicate.setColor("Red");
        duplicate.setDesignStyle("Basic");
        duplicate.setLowStockThreshold(10);

        createRequest.setVariants(List.of(createRequest.getVariants().get(0), duplicate));

        when(productRepository.existsByProductCode("PROD001")).thenReturn(false);

        assertThatThrownBy(() -> productService.createProduct(createRequest, 99L))
                .isInstanceOf(DuplicateVariantSkuException.class)
                .hasMessage("Duplicate variant combination in request: PROD001-S-RED-BASIC");
    }

    @Test
    void createProduct_shouldThrow_whenGeneratedSkuAlreadyExists() {
        ProductVariant existingVariant = ProductVariant.builder().id(400L).productId(88L).sku("PROD001-S-RED-BASIC").build();

        when(productRepository.existsByProductCode("PROD001")).thenReturn(false);
        when(variantRepository.findBySku("PROD001-S-RED-BASIC")).thenReturn(Optional.of(existingVariant));

        assertThatThrownBy(() -> productService.createProduct(createRequest, 99L))
                .isInstanceOf(DuplicateVariantSkuException.class)
                .hasMessage("Generated SKU already exists: PROD001-S-RED-BASIC");
    }

    @Test
    void createProduct_shouldCreateProductAndInitializeInventory_whenRequestValid() {
        when(productRepository.existsByProductCode("PROD001")).thenReturn(false);
        when(variantRepository.findBySku("PROD001-S-RED-BASIC")).thenReturn(Optional.empty());
        when(variantRepository.findBySku("PROD001-M-BLUE")).thenReturn(Optional.empty());
        when(inventoryService.getCurrentQuantity(1001L)).thenReturn(0);
        when(inventoryService.getCurrentQuantity(1002L)).thenReturn(3);

        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product saved = invocation.getArgument(0);
            saved.setId(55L);
            saved.setCreatedAt(ZonedDateTime.now());
            saved.setUpdatedAt(ZonedDateTime.now());
            saved.getVariants().get(0).setId(1001L);
            saved.getVariants().get(0).setCreatedAt(ZonedDateTime.now());
            saved.getVariants().get(0).setUpdatedAt(ZonedDateTime.now());
            saved.getVariants().get(1).setId(1002L);
            saved.getVariants().get(1).setCreatedAt(ZonedDateTime.now());
            saved.getVariants().get(1).setUpdatedAt(ZonedDateTime.now());
            return saved;
        });

        ProductResponse response = productService.createProduct(createRequest, 99L);

        assertThat(response.getId()).isEqualTo(55L);
        assertThat(response.getVariants()).hasSize(2);
        assertThat(response.getVariants().get(0).getSku()).isEqualTo("PROD001-S-RED-BASIC");
        assertThat(response.getVariants().get(1).getSku()).isEqualTo("PROD001-M-BLUE");
        assertThat(response.getVariants().get(0).getVariantPriceVnd()).isEqualTo(120000L);
        assertThat(response.getVariants().get(1).getVariantPriceVnd()).isEqualTo(150000L);
        assertThat(response.getVariants().get(0).getLowStock()).isTrue();

        verify(inventoryService).initializeZeroStockForVariants(List.of(1001L, 1002L));
    }

    @Test
    void getProducts_shouldReturnPage_whenNoFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        JpaProduct withNullVariants = JpaProduct.builder()
                .id(1L)
                .productCode("P1")
                .nameVn("One")
                .basePriceVnd(100L)
                .isActive(true)
                .variants(null)
                .build();

        JpaProduct withTwoVariants = JpaProduct.builder()
                .id(2L)
                .productCode("P2")
                .nameVn("Two")
                .basePriceVnd(200L)
                .isActive(true)
                .variants(new ArrayList<>())
                .build();
        withTwoVariants.getVariants().add(JpaProductVariant.builder().id(10L).build());
        withTwoVariants.getVariants().add(JpaProductVariant.builder().id(11L).build());

        when(springDataProductRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(withNullVariants, withTwoVariants), pageable, 2));

        Page<ProductResponse> responses = productService.getProducts(pageable, null, null, null);

        assertThat(responses.getTotalElements()).isEqualTo(2);
        assertThat(responses.getContent().get(0).getVariantCount()).isEqualTo(0);
        assertThat(responses.getContent().get(1).getVariantCount()).isEqualTo(2);
    }

    @Test
    void getProducts_shouldHandleAllFilterBranches() {
        Pageable pageable = PageRequest.of(0, 10);
        when(springDataProductRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        productService.getProducts(pageable, 10L, true, "shirt");
        productService.getProducts(pageable, null, false, "   ");

        verify(springDataProductRepository, times(2)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getProducts_shouldBuildSearchPredicate_whenSearchProvided() {
        Pageable pageable = PageRequest.of(0, 10);
        ArgumentCaptor<Specification<JpaProduct>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        when(springDataProductRepository.findAll(specCaptor.capture(), eq(pageable))).thenReturn(Page.empty(pageable));

        productService.getProducts(pageable, null, null, "shirt");

        @SuppressWarnings("unchecked")
        Root<JpaProduct> root = (Root<JpaProduct>) mock(Root.class);
        @SuppressWarnings("unchecked")
        Path<Object> productCodePath = (Path<Object>) mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> nameVnPath = (Path<Object>) mock(Path.class);
        @SuppressWarnings("unchecked")
        Expression<String> lowerCode = (Expression<String>) mock(Expression.class);
        @SuppressWarnings("unchecked")
        Expression<String> lowerName = (Expression<String>) mock(Expression.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        Predicate p1 = mock(Predicate.class);
        Predicate p2 = mock(Predicate.class);
        Predicate pOr = mock(Predicate.class);
        Predicate pConjunction = mock(Predicate.class);

        when(root.get("productCode")).thenReturn(productCodePath);
        when(root.get("nameVn")).thenReturn(nameVnPath);
        when(cb.conjunction()).thenReturn(pConjunction);
        when(cb.lower(any(Expression.class))).thenReturn(lowerCode, lowerName);
        when(cb.like(lowerCode, "%shirt%")).thenReturn(p1);
        when(cb.like(lowerName, "%shirt%")).thenReturn(p2);
        when(cb.or(p1, p2)).thenReturn(pOr);
        when(cb.and(any(Predicate.class), any(Predicate.class))).thenReturn(pOr);

        specCaptor.getValue().toPredicate(root, query, cb);

        verify(cb).like(lowerCode, "%shirt%");
        verify(cb).like(lowerName, "%shirt%");
        verify(cb).or(p1, p2);
    }

    @Test
    void getProductById_shouldThrow_whenProductNotFound() {
        when(productRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(404L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product not found with id: 404");
    }

    @Test
    void getProductById_shouldMapProductWithoutVariants() {
        Product product = Product.builder()
                .id(44L)
                .productCode("P-44")
                .nameVn("No Variant")
                .basePriceVnd(50000L)
                .variants(null)
                .build();

        when(productRepository.findById(44L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(44L);

        assertThat(response.getId()).isEqualTo(44L);
        assertThat(response.getVariants()).isNull();
    }

    @Test
    void getProductById_shouldHandleNullQuantityAndThreshold() {
        ProductVariant variant = ProductVariant.builder()
                .id(501L)
                .sku("P-501")
                .size("S")
                .color("Red")
                .lowStockThreshold(null)
                .isActive(true)
                .build();
        Product product = Product.builder()
                .id(45L)
                .productCode("P-45")
                .nameVn("Variant Product")
                .basePriceVnd(50000L)
                .variants(List.of(variant))
                .build();

        when(productRepository.findById(45L)).thenReturn(Optional.of(product));
        when(inventoryService.getCurrentQuantity(501L)).thenReturn(null);

        ProductResponse response = productService.getProductById(45L);

        assertThat(response.getVariants()).hasSize(1);
        assertThat(response.getVariants().getFirst().getCurrentQuantity()).isNull();
        assertThat(response.getVariants().getFirst().getLowStock()).isFalse();
    }

    @Test
    void createProduct_shouldGenerateSkuWithoutOptionalParts_whenValuesBlankOrNull() {
        CreateProductRequest request = new CreateProductRequest();
        request.setProductCode("P-OPT");
        request.setNameVn("Optional");
        request.setBasePriceVnd(1000L);

        VariantDto variantDto = new VariantDto();
        variantDto.setSize(" ");
        variantDto.setColor(null);
        variantDto.setDesignStyle(" ");
        variantDto.setVariantPriceVnd(null);
        request.setVariants(List.of(variantDto));

        when(productRepository.existsByProductCode("P-OPT")).thenReturn(false);
        when(variantRepository.findBySku("P-OPT")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product saved = invocation.getArgument(0);
            saved.setId(77L);
            saved.getVariants().getFirst().setId(7701L);
            return saved;
        });
        when(inventoryService.getCurrentQuantity(7701L)).thenReturn(0);

        ProductResponse response = productService.createProduct(request, 1L);

        assertThat(response.getVariants().getFirst().getSku()).isEqualTo("P-OPT");
    }

    @Test
    void createProduct_shouldHandleNullAndBlankOptionalSkuParts() {
        CreateProductRequest request = new CreateProductRequest();
        request.setProductCode("P-MIX");
        request.setNameVn("Mixed");
        request.setBasePriceVnd(1000L);

        VariantDto variantDto = new VariantDto();
        variantDto.setSize(null);
        variantDto.setColor(" ");
        variantDto.setDesignStyle(null);
        request.setVariants(List.of(variantDto));

        when(productRepository.existsByProductCode("P-MIX")).thenReturn(false);
        when(variantRepository.findBySku("P-MIX")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product saved = invocation.getArgument(0);
            saved.setId(78L);
            saved.getVariants().getFirst().setId(7801L);
            return saved;
        });
        when(inventoryService.getCurrentQuantity(7801L)).thenReturn(0);

        ProductResponse response = productService.createProduct(request, 1L);

        assertThat(response.getVariants().getFirst().getSku()).isEqualTo("P-MIX");
    }

    @Test
    void getProductById_shouldMarkLowStockFalse_whenQuantityMeetsThreshold() {
        ProductVariant variant = ProductVariant.builder()
                .id(601L)
                .sku("P-601")
                .size("L")
                .color("Black")
                .lowStockThreshold(5)
                .isActive(true)
                .build();
        Product product = Product.builder()
                .id(46L)
                .productCode("P-46")
                .nameVn("Threshold Product")
                .basePriceVnd(50000L)
                .variants(List.of(variant))
                .build();

        when(productRepository.findById(46L)).thenReturn(Optional.of(product));
        when(inventoryService.getCurrentQuantity(601L)).thenReturn(5);

        ProductResponse response = productService.getProductById(46L);

        assertThat(response.getVariants()).hasSize(1);
        assertThat(response.getVariants().getFirst().getLowStock()).isFalse();
    }

    @Test
    void updateProduct_shouldReplaceVariantMatrix_andInitializeInventory() {
        Product existing = Product.builder()
                .id(88L)
                .productCode("PROD001")
                .nameVn("Old")
                .basePriceVnd(100000L)
                .variants(new ArrayList<>())
                .build();

        ProductVariant legacy = ProductVariant.builder()
                .id(7001L)
                .productId(88L)
                .sku("PROD001-S-RED-BASIC")
                .isActive(true)
                .build();
        existing.getVariants().add(legacy);

        when(productRepository.findById(88L)).thenReturn(Optional.of(existing));
        when(variantRepository.findBySku("PROD001-S-RED-BASIC")).thenReturn(Optional.of(legacy));
        when(variantRepository.findBySku("PROD001-M-BLUE")).thenReturn(Optional.empty());
        when(inventoryService.getCurrentQuantity(9001L)).thenReturn(0);
        when(inventoryService.getCurrentQuantity(9002L)).thenReturn(0);

        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product saved = invocation.getArgument(0);
            saved.getVariants().get(0).setId(9001L);
            saved.getVariants().get(0).setCreatedAt(ZonedDateTime.now());
            saved.getVariants().get(0).setUpdatedAt(ZonedDateTime.now());
            saved.getVariants().get(1).setId(9002L);
            saved.getVariants().get(1).setCreatedAt(ZonedDateTime.now());
            saved.getVariants().get(1).setUpdatedAt(ZonedDateTime.now());
            return saved;
        });

        ProductResponse response = productService.updateProduct(88L, createRequest);

        assertThat(response.getNameVn()).isEqualTo("Ao thun");
        assertThat(response.getVariants()).hasSize(2);
        verify(inventoryService).initializeZeroStockForVariants(List.of(9001L, 9002L));
    }

    @Test
    void updateProduct_shouldThrow_whenSkuExistsOnDifferentProduct() {
        Product existing = Product.builder().id(88L).productCode("PROD001").variants(new ArrayList<>()).build();
        ProductVariant foreign = ProductVariant.builder().id(7002L).productId(99L).sku("PROD001-S-RED-BASIC").build();

        when(productRepository.findById(88L)).thenReturn(Optional.of(existing));
        when(variantRepository.findBySku("PROD001-S-RED-BASIC")).thenReturn(Optional.of(foreign));

        assertThatThrownBy(() -> productService.updateProduct(88L, createRequest))
                .isInstanceOf(DuplicateVariantSkuException.class)
                .hasMessage("Generated SKU already exists: PROD001-S-RED-BASIC");
    }

    @Test
    void updateProduct_shouldThrow_whenProductNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(999L, createRequest))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product not found with id: 999");
    }

    @Test
    void updateProduct_shouldThrow_whenDuplicateVariantCombinationInRequest() {
        Product existing = Product.builder().id(88L).productCode("PROD001").variants(new ArrayList<>()).build();
        when(productRepository.findById(88L)).thenReturn(Optional.of(existing));

        VariantDto duplicate = new VariantDto();
        duplicate.setSize("S");
        duplicate.setColor("Red");
        duplicate.setDesignStyle("Basic");

        CreateProductRequest updateRequest = new CreateProductRequest();
        updateRequest.setProductCode("PROD001");
        updateRequest.setNameVn("Ao thun");
        updateRequest.setBasePriceVnd(120000L);
        updateRequest.setVariants(List.of(duplicate, duplicate));

        assertThatThrownBy(() -> productService.updateProduct(88L, updateRequest))
                .isInstanceOf(DuplicateVariantSkuException.class)
                .hasMessage("Duplicate variant combination in request: PROD001-S-RED-BASIC");
    }

    @Test
    void deleteProduct_shouldThrow_whenAnyVariantHasStock() {
        Product existing = Product.builder().id(7L).variants(new ArrayList<>()).isActive(true).build();
        ProductVariant v = ProductVariant.builder().id(300L).sku("P-1").isActive(true).build();
        existing.getVariants().add(v);

        when(productRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(inventoryService.getCurrentQuantity(300L)).thenReturn(5);

        assertThatThrownBy(() -> productService.deleteProduct(7L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot delete product: variant P-1 has remaining stock.");

        verify(productRepository, never()).save(any());
    }

    @Test
    void deleteProduct_shouldSoftDeleteProductAndVariants_whenNoStock() {
        Product existing = Product.builder().id(8L).variants(new ArrayList<>()).isActive(true).build();
        ProductVariant first = ProductVariant.builder().id(301L).sku("P-2").isActive(true).build();
        ProductVariant second = ProductVariant.builder().id(302L).sku("P-3").isActive(true).build();
        existing.getVariants().add(first);
        existing.getVariants().add(second);

        when(productRepository.findById(8L)).thenReturn(Optional.of(existing));
        when(inventoryService.getCurrentQuantity(301L)).thenReturn(null);
        when(inventoryService.getCurrentQuantity(302L)).thenReturn(0);

        productService.deleteProduct(8L);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        assertThat(productCaptor.getValue().getIsActive()).isFalse();
        assertThat(productCaptor.getValue().getVariants()).allMatch(v -> Boolean.FALSE.equals(v.getIsActive()));
    }

    @Test
    void deleteProduct_shouldThrow_whenProductNotFound() {
        when(productRepository.findById(123L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProduct(123L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product not found with id: 123");
    }
}