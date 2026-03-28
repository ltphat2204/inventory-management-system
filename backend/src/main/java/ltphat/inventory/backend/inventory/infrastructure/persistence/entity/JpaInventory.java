package ltphat.inventory.backend.inventory.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.ZonedDateTime;

@Entity
@Table(name = "inventory", indexes = {
    @Index(name = "idx_inventory_variant_id", columnList = "variant_id"),
    @Index(name = "idx_inventory_current_quantity", columnList = "current_quantity")
})
@SequenceGenerator(name = "inventory_id_seq", sequenceName = "inventory_id_seq", allocationSize = 1)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class JpaInventory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inventory_id_seq")
    private Long id;

    @Column(name = "variant_id", nullable = false, unique = true)
    private Long variantId;

    @Column(name = "current_quantity", nullable = false)
    private Integer currentQuantity;

    @Column(name = "total_value_vnd", nullable = false)
    private Long totalValueVnd;

    @Version
    @Column(name = "version")
    private Integer version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        ZonedDateTime now = ZonedDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (currentQuantity == null) currentQuantity = 0;
        if (totalValueVnd == null) totalValueVnd = 0L;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }
}
