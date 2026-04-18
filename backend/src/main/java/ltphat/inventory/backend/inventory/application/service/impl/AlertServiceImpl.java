package ltphat.inventory.backend.inventory.application.service.impl;

import lombok.RequiredArgsConstructor;
import ltphat.inventory.backend.inventory.application.dto.AlertDismissRequest;
import ltphat.inventory.backend.inventory.application.dto.AlertDismissResponse;
import ltphat.inventory.backend.inventory.application.service.IAlertService;
import ltphat.inventory.backend.inventory.domain.exception.AlertNotFoundException;
import ltphat.inventory.backend.inventory.domain.model.AlertType;
import ltphat.inventory.backend.inventory.domain.repository.IInventoryRepository;
import ltphat.inventory.backend.inventory.infrastructure.persistence.entity.JpaDismissedAlert;
import ltphat.inventory.backend.inventory.infrastructure.persistence.repository.SpringDataDismissedAlertRepository;
import ltphat.inventory.backend.shared.security.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements IAlertService {

    private final IInventoryRepository inventoryRepository;
    private final SpringDataDismissedAlertRepository dismissedAlertRepository;

    @Override
    @Transactional
    public AlertDismissResponse dismissAlert(AlertDismissRequest request) {
        return switch (request.getAlertType()) {
            case LOW_STOCK -> dismissLowStockAlert(request.getVariantId());
            case SLOW_MOVING -> throw new IllegalArgumentException("Slow-moving alert dismiss is not implemented yet");
        };
    }

    @Override
    @Transactional
    public AlertDismissResponse dismissLowStockAlert(Long variantId) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long currentUserId = userDetails.getUser().getId();

        if (!inventoryRepository.isVariantLowStock(variantId)) {
            throw new AlertNotFoundException("Low-stock alert not found for variant ID: " + variantId);
        }

        JpaDismissedAlert dismissedAlert = dismissedAlertRepository
                .findByUserIdAndVariantIdAndAlertType(currentUserId, variantId, AlertType.LOW_STOCK)
                .orElseGet(() -> dismissedAlertRepository.save(JpaDismissedAlert.builder()
                        .userId(currentUserId)
                        .variantId(variantId)
                        .alertType(AlertType.LOW_STOCK)
                        .dismissedAt(ZonedDateTime.now())
                        .build()));

        return AlertDismissResponse.builder()
                .variantId(variantId)
                .alertType(AlertType.LOW_STOCK.name())
                .dismissedAt(dismissedAlert.getDismissedAt())
                .build();
    }
}
