package ltphat.inventory.backend.inventory.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import ltphat.inventory.backend.inventory.domain.model.Inventory;
import ltphat.inventory.backend.inventory.domain.repository.InventoryRepository;
import ltphat.inventory.backend.inventory.infrastructure.persistence.entity.JpaInventory;
import ltphat.inventory.backend.inventory.infrastructure.persistence.mapper.InventoryMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class InventoryRepositoryImpl implements InventoryRepository {

    private final SpringDataInventoryRepository springDataRepository;
    private final InventoryMapper mapper;

    @Override
    public Inventory save(Inventory inventory) {
        JpaInventory entity = mapper.toEntity(inventory);
        return mapper.toDomain(springDataRepository.save(entity));
    }

    @Override
    public List<Inventory> saveAll(List<Inventory> inventories) {
        List<JpaInventory> entities = inventories.stream()
                .map(mapper::toEntity)
                .collect(Collectors.toList());
        return springDataRepository.saveAll(entities).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Inventory> findByVariantId(Long variantId) {
        return springDataRepository.findByVariantId(variantId).map(mapper::toDomain);
    }

    @Override
    public Optional<Inventory> findByVariantIdWithLock(Long variantId) {
        return springDataRepository.findByVariantIdWithLock(variantId).map(mapper::toDomain);
    }

    @Override
    public List<Inventory> findByVariantIdIn(List<Long> variantIds) {
        return springDataRepository.findByVariantIdIn(variantIds).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
