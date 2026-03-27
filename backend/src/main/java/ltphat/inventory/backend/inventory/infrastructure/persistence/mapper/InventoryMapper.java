package ltphat.inventory.backend.inventory.infrastructure.persistence.mapper;

import ltphat.inventory.backend.inventory.domain.model.Inventory;
import ltphat.inventory.backend.inventory.infrastructure.persistence.entity.JpaInventory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InventoryMapper {
    Inventory toDomain(JpaInventory entity);
    JpaInventory toEntity(Inventory domain);
}
