package ltphat.inventory.backend.inventory.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;

@Entity
@Table(name = "sale_items")
@SequenceGenerator(name = "sale_items_id_seq", sequenceName = "sale_items_id_seq", allocationSize = 1)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class JpaSaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sale_items_id_seq")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private JpaSale sale;

    @Column(name = "variant_id", nullable = false)
    private Long variantId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price_vnd", nullable = false)
    private Long unitPriceVnd;

    @Column(name = "vat_rate", precision = 5, scale = 2)
    private BigDecimal vatRate;

    @Column(name = "vat_amount_vnd", nullable = false)
    private Long vatAmountVnd;

    @Column(name = "line_total_vnd", nullable = false)
    private Long lineTotalVnd;
}
