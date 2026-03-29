package ltphat.inventory.backend.inventory.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ltphat.inventory.backend.inventory.domain.model.AlertType;
import org.hibernate.envers.Audited;

import java.time.ZonedDateTime;

@Entity
@Table(name = "dismissed_alerts", uniqueConstraints = {
        @UniqueConstraint(name = "uk_dismissed_alerts_user_variant_type", columnNames = {"user_id", "variant_id", "alert_type"})
}, indexes = {
        @Index(name = "idx_dismissed_alerts_user_type", columnList = "user_id, alert_type"),
        @Index(name = "idx_dismissed_alerts_variant", columnList = "variant_id")
})
@SequenceGenerator(name = "dismissed_alerts_id_seq", sequenceName = "dismissed_alerts_id_seq", allocationSize = 1)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class JpaDismissedAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dismissed_alerts_id_seq")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "variant_id", nullable = false)
    private Long variantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false)
    private AlertType alertType;

    @Column(name = "dismissed_at", nullable = false)
    private ZonedDateTime dismissedAt;

    @PrePersist
    protected void onCreate() {
        if (dismissedAt == null) {
            dismissedAt = ZonedDateTime.now();
        }
    }
}
