package ltphat.inventory.backend.inventory.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(name = "stock_imports")
@SequenceGenerator(name = "stock_imports_id_seq", sequenceName = "stock_imports_id_seq", allocationSize = 1)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class JpaStockImport {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stock_imports_id_seq")
    private Long id;

    @Column(name = "import_number", nullable = false, unique = true, length = 50)
    private String importNumber;

    @Column(name = "supplier_name", length = 200)
    private String supplierName;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "import_date")
    private ZonedDateTime importDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @OneToMany(mappedBy = "stockImport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JpaStockImportItem> items;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        ZonedDateTime now = ZonedDateTime.now();
        if (createdAt == null) createdAt = now;
        if (importDate == null) importDate = now;
    }
}
