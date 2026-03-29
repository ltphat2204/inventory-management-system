package ltphat.inventory.backend.inventory.infrastructure.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(name = "sales")
@SequenceGenerator(name = "sales_id_seq", sequenceName = "sales_id_seq", allocationSize = 1)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class JpaSale {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sales_id_seq")
    private Long id;

    @Column(name = "sale_number", nullable = false, unique = true, length = 50)
    private String saleNumber;

    @Column(name = "sale_at")
    private ZonedDateTime saleAt;

    @Column(name = "cashier_id", nullable = false)
    private Long cashierId;

    @Column(name = "subtotal_vnd", nullable = false)
    private Long subtotalVnd;

    @Column(name = "discount_vnd", nullable = false)
    private Long discountVnd;

    @Column(name = "total_vnd", nullable = false)
    private Long totalVnd;

    @Column(name = "e_invoice_exported", nullable = false)
    private Boolean eInvoiceExported;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 64)
    private String idempotencyKey;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JpaSaleItem> items;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        ZonedDateTime now = ZonedDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (saleAt == null) {
            saleAt = now;
        }
        if (discountVnd == null) {
            discountVnd = 0L;
        }
        if (eInvoiceExported == null) {
            eInvoiceExported = false;
        }
    }
}
