package ltphat.inventory.backend.inventory.infrastructure.persistence.mapper;

import ltphat.inventory.backend.inventory.domain.model.InventoryTransaction;
import ltphat.inventory.backend.inventory.infrastructure.persistence.entity.JpaInventoryTransaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InventoryTransactionMapper {
    InventoryTransaction toDomain(JpaInventoryTransaction entity);
    JpaInventoryTransaction toEntity(InventoryTransaction domain);
}
