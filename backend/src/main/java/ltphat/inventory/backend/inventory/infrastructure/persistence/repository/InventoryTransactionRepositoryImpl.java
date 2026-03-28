package ltphat.inventory.backend.inventory.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import ltphat.inventory.backend.inventory.domain.model.InventoryTransaction;
import ltphat.inventory.backend.inventory.domain.repository.InventoryTransactionRepository;
import ltphat.inventory.backend.inventory.infrastructure.persistence.entity.JpaInventoryTransaction;
import ltphat.inventory.backend.inventory.infrastructure.persistence.mapper.InventoryTransactionMapper;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InventoryTransactionRepositoryImpl implements InventoryTransactionRepository {

    private final SpringDataInventoryTransactionRepository springDataRepository;
    private final InventoryTransactionMapper mapper;

    @Override
    public InventoryTransaction save(InventoryTransaction transaction) {
        JpaInventoryTransaction entity = mapper.toEntity(transaction);
        return mapper.toDomain(springDataRepository.save(entity));
    }
}
