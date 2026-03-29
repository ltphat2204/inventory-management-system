package ltphat.inventory.backend.inventory.application.service.impl;

import lombok.RequiredArgsConstructor;
import ltphat.inventory.backend.catalog.domain.exception.ProductNotFoundException;
import ltphat.inventory.backend.catalog.domain.exception.VariantNotFoundException;
import ltphat.inventory.backend.catalog.domain.model.Product;
import ltphat.inventory.backend.catalog.domain.model.ProductVariant;
import ltphat.inventory.backend.catalog.domain.repository.IProductRepository;
import ltphat.inventory.backend.catalog.domain.repository.IProductVariantRepository;
import ltphat.inventory.backend.inventory.application.InventoryApplicationMapper;
import ltphat.inventory.backend.inventory.application.dto.SaleItemRequest;
import ltphat.inventory.backend.inventory.application.dto.SaleRequest;
import ltphat.inventory.backend.inventory.application.dto.SaleResponse;
import ltphat.inventory.backend.inventory.application.service.ISaleService;
import ltphat.inventory.backend.inventory.domain.exception.InsufficientStockException;
import ltphat.inventory.backend.inventory.domain.exception.InventoryNotFoundException;
import ltphat.inventory.backend.inventory.domain.model.Inventory;
import ltphat.inventory.backend.inventory.domain.model.InventoryTransaction;
import ltphat.inventory.backend.inventory.domain.model.MovementType;
import ltphat.inventory.backend.inventory.domain.model.Sale;
import ltphat.inventory.backend.inventory.domain.model.SaleItem;
import ltphat.inventory.backend.inventory.domain.repository.IInventoryRepository;
import ltphat.inventory.backend.inventory.domain.repository.IInventoryTransactionRepository;
import ltphat.inventory.backend.inventory.domain.repository.ISaleRepository;
import ltphat.inventory.backend.shared.api.exception.IdempotencyConflictException;
import ltphat.inventory.backend.shared.security.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements ISaleService {

    private static final DateTimeFormatter SALE_NUMBER_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ISaleRepository saleRepository;
    private final IInventoryRepository inventoryRepository;
    private final IInventoryTransactionRepository inventoryTransactionRepository;
    private final IProductVariantRepository productVariantRepository;
    private final IProductRepository productRepository;
    private final InventoryApplicationMapper inventoryApplicationMapper;

    @Override
    @Transactional
    public SaleResponse createSale(SaleRequest request) {
        if (saleRepository.existsByIdempotencyKey(request.getIdempotencyKey())) {
            throw new IdempotencyConflictException("Duplicate idempotency key: " + request.getIdempotencyKey());
        }

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long cashierId = userDetails.getUser().getId();
        ZonedDateTime now = ZonedDateTime.now();

        Map<Long, Integer> requestedQtyByVariant = aggregateRequestedQuantities(request.getItems());
        List<Long> orderedVariantIds = requestedQtyByVariant.keySet().stream()
                .sorted(Comparator.naturalOrder())
                .toList();

        Map<Long, Product> productMap = new HashMap<>();
        Map<Long, Inventory> lockedInventories = new HashMap<>();
        List<SaleItem> saleItems = new ArrayList<>();

        long subtotal = 0L;
        for (Long variantId : orderedVariantIds) {
            Integer requestedQty = requestedQtyByVariant.get(variantId);

            ProductVariant variant = productVariantRepository.findById(variantId)
                    .orElseThrow(() -> new VariantNotFoundException("Variant not found with id: " + variantId));

            Inventory inventory = inventoryRepository.findByVariantIdWithLock(variantId)
                    .orElseThrow(() -> new InventoryNotFoundException("Inventory record not found for variant ID: " + variantId));
            lockedInventories.put(variantId, inventory);

            Integer currentQty = inventory.getCurrentQuantity();
            if (currentQty < requestedQty) {
                throw new InsufficientStockException("Insufficient stock for variant ID: " + variantId);
            }

            Product product = productMap.computeIfAbsent(variant.getProductId(), productId ->
                    productRepository.findById(productId)
                            .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId))
            );

            Long unitPrice = variant.getVariantPriceVnd() != null ? variant.getVariantPriceVnd() : 0L;
            Long lineTotal = unitPrice * requestedQty;
            BigDecimal vatRate = product.getVatRate() != null ? product.getVatRate() : BigDecimal.ZERO;
            Long vatAmount = calculateVatAmount(lineTotal, vatRate);

            subtotal += lineTotal;
            saleItems.add(SaleItem.builder()
                    .variantId(variantId)
                    .quantity(requestedQty)
                    .unitPriceVnd(unitPrice)
                    .vatRate(vatRate)
                    .vatAmountVnd(vatAmount)
                    .lineTotalVnd(lineTotal)
                    .build());
        }

        long discount = request.getDiscountVnd() == null ? 0L : request.getDiscountVnd();
        if (discount > subtotal) {
            throw new IllegalArgumentException("Discount cannot exceed subtotal");
        }

        Sale sale = Sale.builder()
                .saleNumber(generateSaleNumber(now))
                .saleAt(now)
                .cashierId(cashierId)
                .subtotalVnd(subtotal)
                .discountVnd(discount)
                .totalVnd(subtotal - discount)
                .eInvoiceExported(false)
                .notes(request.getNotes())
                .idempotencyKey(request.getIdempotencyKey())
                .items(saleItems)
                .createdAt(now)
                .build();

        Sale savedSale = saleRepository.save(sale);

        for (SaleItem saleItem : savedSale.getItems()) {
            Inventory inventory = lockedInventories.get(saleItem.getVariantId());
            Integer previousQty = inventory.getCurrentQuantity();
            Integer newQty = previousQty - saleItem.getQuantity();

            inventory.setCurrentQuantity(newQty);
            inventory.setUpdatedAt(now);
            inventoryRepository.save(inventory);

            InventoryTransaction transaction = InventoryTransaction.builder()
                    .variantId(saleItem.getVariantId())
                    .movementType(MovementType.SALE)
                    .quantityChange(-saleItem.getQuantity())
                    .unitPriceVnd(saleItem.getUnitPriceVnd())
                    .saleId(savedSale.getId())
                    .idempotencyKey(savedSale.getIdempotencyKey())
                    .performedAt(now)
                    .quantityChanged(-saleItem.getQuantity())
                    .previousQuantity(previousQty)
                    .newQuantity(newQty)
                    .reason("Sale: " + savedSale.getSaleNumber())
                    .referenceId(savedSale.getId())
                    .userId(cashierId)
                    .createdAt(now)
                    .build();
            inventoryTransactionRepository.save(transaction);
        }

        return inventoryApplicationMapper.toSaleResponse(savedSale);
    }

    private Map<Long, Integer> aggregateRequestedQuantities(List<SaleItemRequest> items) {
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (SaleItemRequest item : items) {
            quantities.merge(item.getVariantId(), item.getQuantity(), Integer::sum);
        }
        return quantities;
    }

    private Long calculateVatAmount(Long lineTotal, BigDecimal vatRate) {
        BigDecimal vat = BigDecimal.valueOf(lineTotal)
                .multiply(vatRate)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        return vat.longValue();
    }

    private String generateSaleNumber(ZonedDateTime now) {
        String timestamp = now.format(SALE_NUMBER_TIME);
        String candidate;
        do {
            int random = ThreadLocalRandom.current().nextInt(1000, 10000);
            candidate = "S-" + timestamp + "-" + random;
        } while (saleRepository.existsBySaleNumber(candidate));
        return candidate;
    }
}
