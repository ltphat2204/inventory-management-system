package ltphat.inventory.backend.inventory.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import ltphat.inventory.backend.inventory.domain.model.InventoryTransaction;
import ltphat.inventory.backend.inventory.domain.repository.IInventoryTransactionRepository;
import ltphat.inventory.backend.inventory.infrastructure.persistence.entity.JpaInventoryTransaction;
import ltphat.inventory.backend.inventory.infrastructure.persistence.mapper.InventoryTransactionMapper;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InventoryTransactionRepositoryAdapter implements IInventoryTransactionRepository {

    private final SpringDataInventoryTransactionRepository springDataRepository;
    private final InventoryTransactionMapper mapper;

    @Override
    public InventoryTransaction save(InventoryTransaction transaction) {
        JpaInventoryTransaction entity = mapper.toEntity(transaction);
        return mapper.toDomain(springDataRepository.save(entity));
    }

    @Override
    public boolean existsByIdempotencyKey(String idempotencyKey) {
        return springDataRepository.existsByIdempotencyKey(idempotencyKey);
    }

    @Override
    public org.springframework.data.domain.Page<InventoryTransaction> findAll(
            org.springframework.data.domain.Pageable pageable, 
            Long userId, 
            java.time.ZonedDateTime start, 
            java.time.ZonedDateTime end, 
            Long variantId, 
            ltphat.inventory.backend.inventory.domain.model.MovementType movementType
    ) {
        org.springframework.data.jpa.domain.Specification<JpaInventoryTransaction> spec = 
            org.springframework.data.jpa.domain.Specification.where((root, query, cb) -> cb.conjunction());

        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("userId"), userId));
        }
        if (variantId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("variantId"), variantId));
        }
        if (movementType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("movementType"), movementType));
        }
        if (start != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("performedAt"), start));
        }
        if (end != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("performedAt"), end));
        }

        return springDataRepository.findAll(spec, pageable).map(mapper::toDomain);
    }
}
