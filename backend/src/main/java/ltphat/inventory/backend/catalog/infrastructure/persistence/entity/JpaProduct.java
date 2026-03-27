package ltphat.inventory.backend.catalog.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@SequenceGenerator(name = "products_id_seq", sequenceName = "products_id_seq", allocationSize = 1)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class JpaProduct {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "products_id_seq")
    private Long id;

    @Column(name = "product_code", nullable = false, length = 50, unique = true)
    private String productCode;

    @Column(name = "name_vn", nullable = false, length = 200)
    private String nameVn;

    @Column(name = "name_en", length = 200)
    private String nameEn;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "base_price_vnd", nullable = false)
    private Long basePriceVnd;

    @Column(name = "vat_rate", precision = 5, scale = 2)
    private BigDecimal vatRate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<JpaProductVariant> variants = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        ZonedDateTime now = ZonedDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (vatRate == null) vatRate = new BigDecimal("10.00");
        if (isActive == null) isActive = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }
    
    public void addVariant(JpaProductVariant variant) {
        variants.add(variant);
        variant.setProduct(this);
    }

    public void removeVariant(JpaProductVariant variant) {
        variants.remove(variant);
        variant.setProduct(null);
    }
}
