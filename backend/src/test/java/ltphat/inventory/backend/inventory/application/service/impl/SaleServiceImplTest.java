package ltphat.inventory.backend.inventory.application.service.impl;

import ltphat.inventory.backend.catalog.domain.model.Product;
import ltphat.inventory.backend.catalog.domain.model.ProductVariant;
import ltphat.inventory.backend.catalog.domain.repository.IProductRepository;
import ltphat.inventory.backend.catalog.domain.repository.IProductVariantRepository;
import ltphat.inventory.backend.iam.domain.model.Role;
import ltphat.inventory.backend.iam.domain.model.User;
import ltphat.inventory.backend.inventory.application.InventoryApplicationMapper;
import ltphat.inventory.backend.inventory.application.dto.SaleItemRequest;
import ltphat.inventory.backend.inventory.application.dto.SaleRequest;
import ltphat.inventory.backend.inventory.application.dto.SaleResponse;
import ltphat.inventory.backend.inventory.domain.exception.InsufficientStockException;
import ltphat.inventory.backend.inventory.domain.model.Inventory;
import ltphat.inventory.backend.inventory.domain.model.Sale;
import ltphat.inventory.backend.inventory.domain.model.SaleItem;
import ltphat.inventory.backend.inventory.domain.repository.IInventoryRepository;
import ltphat.inventory.backend.inventory.domain.repository.IInventoryTransactionRepository;
import ltphat.inventory.backend.inventory.domain.repository.ISaleRepository;
import ltphat.inventory.backend.shared.api.exception.IdempotencyConflictException;
import ltphat.inventory.backend.shared.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaleServiceImplTest {

    @Mock
    private ISaleRepository saleRepository;

    @Mock
    private IInventoryRepository inventoryRepository;

    @Mock
    private IInventoryTransactionRepository inventoryTransactionRepository;

    @Mock
    private IProductVariantRepository productVariantRepository;

    @Mock
    private IProductRepository productRepository;

    @Mock
    private InventoryApplicationMapper inventoryApplicationMapper;

    @InjectMocks
    private SaleServiceImpl saleService;

    @BeforeEach
    void setUpSecurityContext() {
        Role role = Role.builder().id(1L).name("CASHIER").build();
        User user = User.builder().id(20L).username("cashier").role(role).isActive(true).build();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createSale_shouldDeductStockAndCreateTransactions_whenRequestIsValid() {
        SaleRequest request = SaleRequest.builder()
                .idempotencyKey("idem-1")
                .discountVnd(5_000L)
                .items(List.of(
                        SaleItemRequest.builder().variantId(101L).quantity(2).build(),
                        SaleItemRequest.builder().variantId(101L).quantity(1).build(),
                        SaleItemRequest.builder().variantId(102L).quantity(1).build()
                ))
                .build();

        ProductVariant variant101 = ProductVariant.builder().id(101L).productId(11L).variantPriceVnd(100_000L).build();
        ProductVariant variant102 = ProductVariant.builder().id(102L).productId(12L).variantPriceVnd(50_000L).build();
        Product product11 = Product.builder().id(11L).vatRate(new BigDecimal("10.00")).build();
        Product product12 = Product.builder().id(12L).vatRate(new BigDecimal("5.00")).build();

        Inventory inventory101 = Inventory.builder().variantId(101L).currentQuantity(10).totalValueVnd(0L).build();
        Inventory inventory102 = Inventory.builder().variantId(102L).currentQuantity(5).totalValueVnd(0L).build();

        when(saleRepository.existsByIdempotencyKey("idem-1")).thenReturn(false);
        when(saleRepository.existsBySaleNumber(any())).thenReturn(false);
        when(productVariantRepository.findById(101L)).thenReturn(Optional.of(variant101));
        when(productVariantRepository.findById(102L)).thenReturn(Optional.of(variant102));
        when(productRepository.findById(11L)).thenReturn(Optional.of(product11));
        when(productRepository.findById(12L)).thenReturn(Optional.of(product12));
        when(inventoryRepository.findByVariantIdWithLock(101L)).thenReturn(Optional.of(inventory101));
        when(inventoryRepository.findByVariantIdWithLock(102L)).thenReturn(Optional.of(inventory102));

        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> {
            Sale sale = invocation.getArgument(0);
            sale.setId(500L);
            sale.setItems(List.of(
                    SaleItem.builder().variantId(101L).quantity(3).unitPriceVnd(100_000L).lineTotalVnd(300_000L).build(),
                    SaleItem.builder().variantId(102L).quantity(1).unitPriceVnd(50_000L).lineTotalVnd(50_000L).build()
            ));
            return sale;
        });

        SaleResponse mappedResponse = SaleResponse.builder().id(500L).totalVnd(345_000L).build();
        when(inventoryApplicationMapper.toSaleResponse(any(Sale.class))).thenReturn(mappedResponse);

        SaleResponse response = saleService.createSale(request);

        assertThat(response.getId()).isEqualTo(500L);
        assertThat(response.getTotalVnd()).isEqualTo(345_000L);

        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository, times(2)).save(inventoryCaptor.capture());
        List<Inventory> updatedInventories = inventoryCaptor.getAllValues();
        assertThat(updatedInventories.stream().filter(i -> i.getVariantId().equals(101L)).findFirst().orElseThrow().getCurrentQuantity())
                .isEqualTo(7);
        assertThat(updatedInventories.stream().filter(i -> i.getVariantId().equals(102L)).findFirst().orElseThrow().getCurrentQuantity())
                .isEqualTo(4);

        verify(inventoryTransactionRepository, times(2)).save(any());
    }

    @Test
    void createSale_shouldThrow_whenIdempotencyKeyAlreadyExists() {
        SaleRequest request = SaleRequest.builder()
                .idempotencyKey("idem-dup")
                .items(List.of(SaleItemRequest.builder().variantId(1L).quantity(1).build()))
                .build();

        when(saleRepository.existsByIdempotencyKey("idem-dup")).thenReturn(true);

        assertThatThrownBy(() -> saleService.createSale(request))
                .isInstanceOf(IdempotencyConflictException.class)
                .hasMessage("Duplicate idempotency key: idem-dup");

        verify(saleRepository, never()).save(any());
    }

    @Test
    void createSale_shouldThrowAndRollback_whenStockIsInsufficient() {
        SaleRequest request = SaleRequest.builder()
                .idempotencyKey("idem-2")
                .items(List.of(SaleItemRequest.builder().variantId(101L).quantity(2).build()))
                .build();

        ProductVariant variant101 = ProductVariant.builder().id(101L).productId(11L).variantPriceVnd(100_000L).build();
        Inventory inventory101 = Inventory.builder().variantId(101L).currentQuantity(1).totalValueVnd(0L).build();

        when(saleRepository.existsByIdempotencyKey("idem-2")).thenReturn(false);
        when(productVariantRepository.findById(101L)).thenReturn(Optional.of(variant101));
        when(inventoryRepository.findByVariantIdWithLock(101L)).thenReturn(Optional.of(inventory101));

        assertThatThrownBy(() -> saleService.createSale(request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessage("Insufficient stock for variant ID: 101");

        verify(saleRepository, never()).save(any());
        verify(inventoryRepository, never()).save(any());
        verify(inventoryTransactionRepository, never()).save(any());
    }

    @Test
    void createSale_shouldThrow_whenDiscountExceedsSubtotal() {
        SaleRequest request = SaleRequest.builder()
                .idempotencyKey("idem-3")
                .discountVnd(200_000L)
                .items(List.of(SaleItemRequest.builder().variantId(101L).quantity(1).build()))
                .build();

        ProductVariant variant101 = ProductVariant.builder().id(101L).productId(11L).variantPriceVnd(100_000L).build();
        Product product11 = Product.builder().id(11L).vatRate(new BigDecimal("10.00")).build();
        Inventory inventory101 = Inventory.builder().variantId(101L).currentQuantity(5).totalValueVnd(0L).build();

        when(saleRepository.existsByIdempotencyKey("idem-3")).thenReturn(false);
        when(productVariantRepository.findById(101L)).thenReturn(Optional.of(variant101));
        when(productRepository.findById(11L)).thenReturn(Optional.of(product11));
        when(inventoryRepository.findByVariantIdWithLock(101L)).thenReturn(Optional.of(inventory101));

        assertThatThrownBy(() -> saleService.createSale(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Discount cannot exceed subtotal");

        verify(saleRepository, never()).save(any());
    }

        @Test
        void createSale_shouldAllowOnlyOneSuccess_whenTwoCashiersSellLastItemConcurrently() throws Exception {
                ProductVariant variant = ProductVariant.builder().id(101L).productId(11L).variantPriceVnd(100_000L).build();
                Product product = Product.builder().id(11L).vatRate(new BigDecimal("10.00")).build();
                Inventory sharedInventory = Inventory.builder().variantId(101L).currentQuantity(1).totalValueVnd(0L).build();

                when(productVariantRepository.findById(101L)).thenReturn(Optional.of(variant));
                when(productRepository.findById(11L)).thenReturn(Optional.of(product));
                when(saleRepository.existsByIdempotencyKey(any())).thenReturn(false);
                when(saleRepository.existsBySaleNumber(any())).thenReturn(false);

                AtomicInteger findCallCount = new AtomicInteger(0);
                CountDownLatch firstThreadPassedLock = new CountDownLatch(1);
                CountDownLatch firstThreadSaved = new CountDownLatch(1);

                when(inventoryRepository.findByVariantIdWithLock(101L)).thenAnswer(invocation -> {
                        int call = findCallCount.incrementAndGet();
                        if (call == 1) {
                                firstThreadPassedLock.countDown();
                                return Optional.of(sharedInventory);
                        }
                        firstThreadSaved.await(2, TimeUnit.SECONDS);
                        return Optional.of(sharedInventory);
                });

                when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> {
                        Inventory updated = invocation.getArgument(0);
                        sharedInventory.setCurrentQuantity(updated.getCurrentQuantity());
                        firstThreadSaved.countDown();
                        return sharedInventory;
                });

                AtomicLong saleIdGenerator = new AtomicLong(1000L);
                when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> {
                        Sale sale = invocation.getArgument(0);
                        sale.setId(saleIdGenerator.getAndIncrement());
                        return sale;
                });

                when(inventoryApplicationMapper.toSaleResponse(any(Sale.class))).thenAnswer(invocation -> {
                        Sale sale = invocation.getArgument(0);
                        return SaleResponse.builder()
                                        .id(sale.getId())
                                        .saleNumber(sale.getSaleNumber())
                                        .totalVnd(sale.getTotalVnd())
                                        .build();
                });

                ExecutorService executor = Executors.newFixedThreadPool(2);
                try {
                        Future<SaleResponse> first = executor.submit(() -> executeSaleWithCashier("idem-c1", 21L));
                        firstThreadPassedLock.await(2, TimeUnit.SECONDS);
                        Future<SaleResponse> second = executor.submit(() -> executeSaleWithCashier("idem-c2", 22L));

                        int success = 0;
                        int insufficient = 0;

                        for (Future<SaleResponse> future : List.of(first, second)) {
                                try {
                                        SaleResponse response = future.get(3, TimeUnit.SECONDS);
                                        assertThat(response).isNotNull();
                                        success++;
                                } catch (ExecutionException ex) {
                                        if (ex.getCause() instanceof InsufficientStockException) {
                                                insufficient++;
                                        } else {
                                                throw ex;
                                        }
                                }
                        }

                        assertThat(success).isEqualTo(1);
                        assertThat(insufficient).isEqualTo(1);
                        assertThat(sharedInventory.getCurrentQuantity()).isEqualTo(0);
                } finally {
                        executor.shutdownNow();
                }
        }

        private SaleResponse executeSaleWithCashier(String idempotencyKey, Long cashierId) {
                Role role = Role.builder().id(1L).name("CASHIER").build();
                User user = User.builder().id(cashierId).username("cashier-" + cashierId).role(role).isActive(true).build();
                CustomUserDetails userDetails = new CustomUserDetails(user);
                UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
                try {
                        SaleRequest request = SaleRequest.builder()
                                        .idempotencyKey(idempotencyKey)
                                        .items(List.of(SaleItemRequest.builder().variantId(101L).quantity(1).build()))
                                        .build();
                        return saleService.createSale(request);
                } finally {
                        SecurityContextHolder.clearContext();
                }
        }
}
