package ltphat.inventory.backend.inventory.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.ZonedDateTime;

@Entity
@Table(name = "inventory_transactions")
@SequenceGenerator(name = "inventory_transactions_id_seq", sequenceName = "inventory_transactions_id_seq", allocationSize = 1)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class JpaInventoryTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inventory_transactions_id_seq")
    private Long id;

    @Column(name = "variant_id", nullable = false)
    private Long variantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    private JpaMovementType movementType;

    @Column(name = "adjustment_subtype")
    private String adjustmentSubtype;

    @Column(name = "quantity_change")
    private Integer quantityChange;

    @Column(name = "unit_price_vnd")
    private Long unitPriceVnd;

    @Column(name = "import_id")
    private Long importId;

    @Column(name = "sale_id")
    private Long saleId;

    @Column(name = "idempotency_key", unique = true, length = 64)
    private String idempotencyKey;

    @Column(name = "performed_at")
    private ZonedDateTime performedAt;

    @Column(name = "quantity_changed", nullable = false)
    private Integer quantityChanged;

    @Column(name = "previous_quantity", nullable = false)
    private Integer previousQuantity;

    @Column(name = "new_quantity", nullable = false)
    private Integer newQuantity;

    @Column(name = "reason")
    private String reason;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        ZonedDateTime now = ZonedDateTime.now();
        if (createdAt == null) createdAt = now;
        if (performedAt == null) performedAt = now;
    }
}
