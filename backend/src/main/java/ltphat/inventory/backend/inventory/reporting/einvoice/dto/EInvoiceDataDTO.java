package ltphat.inventory.backend.inventory.reporting.einvoice.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "EInvoice")
public class EInvoiceDataDTO {

    @JacksonXmlProperty(localName = "SaleNumber")
    private String saleNumber;

    @JacksonXmlProperty(localName = "SaleDate")
    private ZonedDateTime saleAt;

    @JacksonXmlProperty(localName = "SubtotalVnd")
    private Long subtotalVnd;

    @JacksonXmlProperty(localName = "DiscountVnd")
    private Long discountVnd;

    @JacksonXmlProperty(localName = "TotalVnd")
    private Long totalVnd;

    @JacksonXmlElementWrapper(localName = "Items")
    @JacksonXmlProperty(localName = "Item")
    private List<EInvoiceItemDTO> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EInvoiceItemDTO {

        @JacksonXmlProperty(localName = "VariantSku")
        private String variantSku;

        @JacksonXmlProperty(localName = "ProductName")
        private String productName;

        @JacksonXmlProperty(localName = "Quantity")
        private Integer quantity;

        @JacksonXmlProperty(localName = "UnitPriceVnd")
        private Long unitPriceVnd;

        @JacksonXmlProperty(localName = "VatRate")
        private BigDecimal vatRate;

        @JacksonXmlProperty(localName = "VatAmountVnd")
        private Long vatAmountVnd;

        @JacksonXmlProperty(localName = "LineTotalVnd")
        private Long lineTotalVnd;
    }
}
