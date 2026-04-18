package ltphat.inventory.backend.inventory.application.service;

import ltphat.inventory.backend.inventory.application.dto.AlertDismissRequest;
import ltphat.inventory.backend.inventory.application.dto.AlertDismissResponse;

public interface IAlertService {
    AlertDismissResponse dismissLowStockAlert(Long variantId);

    AlertDismissResponse dismissAlert(AlertDismissRequest request);
}
