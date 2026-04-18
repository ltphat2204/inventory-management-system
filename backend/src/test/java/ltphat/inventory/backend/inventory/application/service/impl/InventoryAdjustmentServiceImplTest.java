package ltphat.inventory.backend.inventory.application.service.impl;

import ltphat.inventory.backend.iam.domain.model.Role;
import ltphat.inventory.backend.iam.domain.model.User;
import ltphat.inventory.backend.inventory.application.dto.InventoryAdjustmentItemRequest;
import ltphat.inventory.backend.inventory.application.dto.InventoryAdjustmentRequest;
import ltphat.inventory.backend.inventory.application.dto.InventoryAdjustmentResponse;
import ltphat.inventory.backend.inventory.domain.exception.InsufficientStockException;
import ltphat.inventory.backend.inventory.domain.model.AdjustmentType;
import ltphat.inventory.backend.inventory.domain.model.Inventory;
import ltphat.inventory.backend.inventory.domain.model.InventoryTransaction;
import ltphat.inventory.backend.inventory.domain.repository.IInventoryRepository;
import ltphat.inventory.backend.inventory.domain.repository.IInventoryTransactionRepository;
import ltphat.inventory.backend.shared.api.exception.IdempotencyConflictException;
import ltphat.inventory.backend.shared.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryAdjustmentServiceImplTest {

    @Mock
    private IInventoryRepository inventoryRepository;

    @Mock
    private IInventoryTransactionRepository inventoryTransactionRepository;

    @InjectMocks
    private InventoryAdjustmentServiceImpl inventoryAdjustmentService;

    @BeforeEach
    void setUpSecurityContext() {
        Role role = Role.builder().id(1L).name("MANAGER").build();
        User user = User.builder().id(22L).username("manager").role(role).isActive(true).build();
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
    void adjustStock_shouldApplyAdjustmentAndRecordTransaction() {
        InventoryAdjustmentRequest request = InventoryAdjustmentRequest.builder()
                .idempotencyKey("adj-1")
                .items(List.of(InventoryAdjustmentItemRequest.builder()
                        .variantId(101L)
                        .quantityChange(-2)
                        .adjustmentType(AdjustmentType.DAMAGE)
                        .reason("Damaged during handling")
                        .build()))
                .build();

        Inventory inventory = Inventory.builder().variantId(101L).currentQuantity(10).totalValueVnd(0L).build();

        when(inventoryTransactionRepository.existsByIdempotencyKey("adj-1")).thenReturn(false);
        when(inventoryRepository.findByVariantIdWithLock(101L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryAdjustmentResponse response = inventoryAdjustmentService.adjustStock(request);

        assertThat(response.getIdempotencyKey()).isEqualTo("adj-1");
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getNewQuantity()).isEqualTo(8);
        verify(inventoryTransactionRepository).save(any());
    }

    @Test
    void adjustStock_shouldRejectDuplicateIdempotencyKey() {
        InventoryAdjustmentRequest request = InventoryAdjustmentRequest.builder()
                .idempotencyKey("adj-dup")
                .items(List.of(InventoryAdjustmentItemRequest.builder()
                        .variantId(101L)
                        .quantityChange(1)
                        .adjustmentType(AdjustmentType.CORRECTION)
                        .reason("Manual correction note")
                        .build()))
                .build();

        when(inventoryTransactionRepository.existsByIdempotencyKey("adj-dup")).thenReturn(true);

        assertThatThrownBy(() -> inventoryAdjustmentService.adjustStock(request))
                .isInstanceOf(IdempotencyConflictException.class)
                .hasMessage("Duplicate idempotency key: adj-dup");

        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void adjustStock_shouldRejectNegativeStockResult() {
        InventoryAdjustmentRequest request = InventoryAdjustmentRequest.builder()
                .idempotencyKey("adj-2")
                .items(List.of(InventoryAdjustmentItemRequest.builder()
                        .variantId(101L)
                        .quantityChange(-5)
                        .adjustmentType(AdjustmentType.INTERNAL_USE)
                        .reason("Internal use for store demo")
                        .build()))
                .build();

        Inventory inventory = Inventory.builder().variantId(101L).currentQuantity(2).totalValueVnd(0L).build();

        when(inventoryTransactionRepository.existsByIdempotencyKey("adj-2")).thenReturn(false);
        when(inventoryRepository.findByVariantIdWithLock(101L)).thenReturn(Optional.of(inventory));

        assertThatThrownBy(() -> inventoryAdjustmentService.adjustStock(request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessage("Adjustment would make stock negative for variant ID: 101");
    }

    @Test
    void adjustStock_shouldIncreaseStock_forPositiveReturn() {
        InventoryAdjustmentRequest request = InventoryAdjustmentRequest.builder()
                .idempotencyKey("adj-return")
                .items(List.of(InventoryAdjustmentItemRequest.builder()
                        .variantId(200L)
                        .quantityChange(3)
                        .adjustmentType(AdjustmentType.RETURN)
                        .reason("Customer returned faulty item")
                        .build()))
                .build();

        Inventory inventory = Inventory.builder().variantId(200L).currentQuantity(5).totalValueVnd(0L).build();

        when(inventoryTransactionRepository.existsByIdempotencyKey("adj-return")).thenReturn(false);
        when(inventoryRepository.findByVariantIdWithLock(200L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryAdjustmentResponse response = inventoryAdjustmentService.adjustStock(request);

        assertThat(response.getItems().get(0).getNewQuantity()).isEqualTo(8);
        verify(inventoryTransactionRepository).save(argThat((InventoryTransaction t) ->
                t.getQuantityChange() == 3 && "RETURN".equals(t.getAdjustmentSubtype())));
    }

    @Test
    void adjustStock_multiItem_shouldUseSuffixedIdempotencyKeys() {
        InventoryAdjustmentRequest request = InventoryAdjustmentRequest.builder()
                .idempotencyKey("batch-1")
                .items(List.of(
                        InventoryAdjustmentItemRequest.builder()
                                .variantId(10L)
                                .quantityChange(-1)
                                .adjustmentType(AdjustmentType.DAMAGE)
                                .reason("Damaged during handling")
                                .build(),
                        InventoryAdjustmentItemRequest.builder()
                                .variantId(20L)
                                .quantityChange(1)
                                .adjustmentType(AdjustmentType.CORRECTION)
                                .reason("Physical count correction")
                                .build()))
                .build();

        Inventory inv10 = Inventory.builder().variantId(10L).currentQuantity(5).totalValueVnd(0L).build();
        Inventory inv20 = Inventory.builder().variantId(20L).currentQuantity(3).totalValueVnd(0L).build();

        when(inventoryTransactionRepository.existsByIdempotencyKey("batch-1")).thenReturn(false);
        when(inventoryRepository.findByVariantIdWithLock(10L)).thenReturn(Optional.of(inv10));
        when(inventoryRepository.findByVariantIdWithLock(20L)).thenReturn(Optional.of(inv20));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        inventoryAdjustmentService.adjustStock(request);

        verify(inventoryTransactionRepository, times(2)).save(any());
        verify(inventoryTransactionRepository).save(argThat((InventoryTransaction t) -> "batch-1".equals(t.getIdempotencyKey())));
        verify(inventoryTransactionRepository).save(argThat((InventoryTransaction t) -> "batch-1#1".equals(t.getIdempotencyKey())));
    }
}
