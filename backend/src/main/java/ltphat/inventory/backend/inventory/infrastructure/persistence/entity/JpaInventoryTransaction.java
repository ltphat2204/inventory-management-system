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
        if (createdAt == null) createdAt = ZonedDateTime.now();
    }
}
