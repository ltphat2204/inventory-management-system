package ltphat.inventory.backend.inventory.infrastructure.persistence.repository;

import java.time.ZonedDateTime;

public interface SlowMovingProjection {
    Long getVariantId();
    String getVariantSku();
    String getProductName();
    Integer getCurrentQuantity();
    ZonedDateTime getLastMovementAt();
}
