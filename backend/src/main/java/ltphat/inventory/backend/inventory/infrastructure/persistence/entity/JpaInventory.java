package ltphat.inventory.backend.inventory.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.ZonedDateTime;

@Entity
@Table(name = "inventory")
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

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        ZonedDateTime now = ZonedDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (quantity == null) quantity = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }
}
