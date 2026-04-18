package ltphat.inventory.backend.inventory.application.service;

import lombok.RequiredArgsConstructor;
import ltphat.inventory.backend.catalog.domain.model.Product;
import ltphat.inventory.backend.catalog.domain.model.ProductVariant;
import ltphat.inventory.backend.catalog.domain.repository.IProductRepository;
import ltphat.inventory.backend.catalog.domain.repository.IProductVariantRepository;
import ltphat.inventory.backend.inventory.domain.model.Sale;
import ltphat.inventory.backend.inventory.domain.model.SaleItem;
import ltphat.inventory.backend.inventory.domain.repository.IReportRepository;
import ltphat.inventory.backend.inventory.domain.repository.ISaleRepository;
import ltphat.inventory.backend.inventory.reporting.einvoice.dto.EInvoiceDataDTO;
import ltphat.inventory.backend.inventory.reporting.inout.dto.InOutStockRawData;
import ltphat.inventory.backend.inventory.reporting.inout.dto.InOutStockReportDTO;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportingServiceImpl implements IReportingService {

    private final IReportRepository reportRepository;
    private final ISaleRepository saleRepository;
    private final IProductVariantRepository variantRepository;
    private final IProductRepository productRepository;

    @Override
    public List<InOutStockReportDTO> generateInOutStockReport(ZonedDateTime startDate, ZonedDateTime endDate, List<Long> variantIds) {
        List<InOutStockRawData> rawData = reportRepository.calculateInOutStock(startDate, endDate, variantIds);
        
        List<Long> vIds = rawData.stream().map(InOutStockRawData::getVariantId).collect(Collectors.toList());
        Map<Long, ProductVariant> variantMap = variantRepository.findAllById(vIds).stream()
                .collect(Collectors.toMap(ProductVariant::getId, v -> v));
                
        List<Long> pIds = variantMap.values().stream().map(ProductVariant::getProductId).distinct().collect(Collectors.toList());
        Map<Long, Product> productMap = productRepository.findAllById(pIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        return rawData.stream().map(raw -> {
            ProductVariant variant = variantMap.get(raw.getVariantId());
            Product product = (variant != null) ? productMap.get(variant.getProductId()) : null;
            
            String sku = variant != null ? variant.getSku() : "N/A";
            String details = variant != null ? String.format("Size: %s, Color: %s", variant.getSize(), variant.getColor()) : "N/A";
            String nameVn = product != null ? product.getNameVn() : "N/A";
            String nameEn = product != null ? product.getNameEn() : "N/A";
            
            // For simple reporting we use variant price
            Long price = (variant != null && variant.getVariantPriceVnd() != null) 
                            ? variant.getVariantPriceVnd() 
                            : (product != null ? product.getBasePriceVnd() : 0L);

            return InOutStockReportDTO.builder()
                    .variantId(raw.getVariantId())
                    .sku(sku)
                    .productNameVn(nameVn)
                    .productNameEn(nameEn)
                    .variantDetails(details)
                    .beginningQty(raw.getBeginningQty())
                    .beginningValueVnd(raw.getBeginningQty() * price)
                    .importQty(raw.getImportQty())
                    .importValueVnd(raw.getImportQty() * price)
                    .exportQty(raw.getExportQty())
                    .exportValueVnd(raw.getExportQty() * price)
                    .endingQty(raw.getEndingQty())
                    .endingValueVnd(raw.getEndingQty() * price)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public List<EInvoiceDataDTO> generateEInvoiceData(List<Long> saleIds) {
        List<Sale> sales = saleRepository.findAllById(saleIds);
        
        List<Long> vIds = sales.stream()
                .flatMap(s -> s.getItems().stream())
                .map(SaleItem::getVariantId)
                .distinct()
                .collect(Collectors.toList());
                
        Map<Long, ProductVariant> variantMap = variantRepository.findAllById(vIds).stream()
                .collect(Collectors.toMap(ProductVariant::getId, v -> v));
                
        List<Long> pIds = variantMap.values().stream().map(ProductVariant::getProductId).distinct().collect(Collectors.toList());
        Map<Long, Product> productMap = productRepository.findAllById(pIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        return sales.stream().map(sale -> {
            List<EInvoiceDataDTO.EInvoiceItemDTO> itemDTOs = sale.getItems().stream().map(item -> {
                ProductVariant variant = variantMap.get(item.getVariantId());
                Product product = (variant != null) ? productMap.get(variant.getProductId()) : null;
                
                String sku = variant != null ? variant.getSku() : "N/A";
                String productName = product != null ? product.getNameVn() : "N/A";

                return EInvoiceDataDTO.EInvoiceItemDTO.builder()
                        .variantSku(sku)
                        .productName(productName)
                        .quantity(item.getQuantity())
                        .unitPriceVnd(item.getUnitPriceVnd())
                        .vatRate(item.getVatRate())
                        .vatAmountVnd(item.getVatAmountVnd())
                        .lineTotalVnd(item.getLineTotalVnd())
                        .build();
            }).collect(Collectors.toList());

            return EInvoiceDataDTO.builder()
                    .saleNumber(sale.getSaleNumber())
                    .saleAt(sale.getSaleAt())
                    .subtotalVnd(sale.getSubtotalVnd())
                    .discountVnd(sale.getDiscountVnd())
                    .totalVnd(sale.getTotalVnd())
                    .items(itemDTOs)
                    .build();
        }).collect(Collectors.toList());
    }
}
