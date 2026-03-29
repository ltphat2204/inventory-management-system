package ltphat.inventory.backend.inventory.application.service;

import ltphat.inventory.backend.inventory.application.dto.DashboardResponse;

public interface IDashboardService {
    DashboardResponse getDashboard(int lowStockLimit);
}
