package ltphat.inventory.backend.inventory.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "stock_import_items")
@SequenceGenerator(name = "stock_import_items_id_seq", sequenceName = "stock_import_items_id_seq", allocationSize = 1)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class JpaStockImportItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stock_import_items_id_seq")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "import_id", nullable = false)
    private JpaStockImport stockImport;

    @Column(name = "variant_id", nullable = false)
    private Long variantId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_cost_vnd", nullable = false)
    private Long unitCostVnd;

    @Column(name = "line_total_vnd", nullable = false)
    private Long lineTotalVnd;
}
