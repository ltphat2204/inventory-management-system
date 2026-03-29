package ltphat.inventory.backend.inventory.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private long totalSkuCount;
    private long totalStockValueVnd;
    private long lowStockCount;
    private long todaysSalesTotalVnd;
    private List<InventoryOverviewResponse> lowStockItems;
}
