package ltphat.inventory.backend.inventory.application.service;

import ltphat.inventory.backend.inventory.application.dto.InventoryTransactionResponse;
import ltphat.inventory.backend.inventory.domain.model.MovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.ZonedDateTime;

public interface IInventoryTransactionService {
    Page<InventoryTransactionResponse> getTransactions(Pageable pageable, Long userId, ZonedDateTime start, ZonedDateTime end, Long variantId, MovementType movementType);
}
