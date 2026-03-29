package ltphat.inventory.backend.inventory.application.service;

import ltphat.inventory.backend.inventory.application.dto.SaleRequest;
import ltphat.inventory.backend.inventory.application.dto.SaleResponse;

public interface ISaleService {
    SaleResponse createSale(SaleRequest request);
}
