package ltphat.inventory.backend.iam.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    private Long id;
    private User user;
    private String token;
    private LocalDateTime expiryDate;
    
    public boolean isExpired() {
        return expiryDate.isBefore(LocalDateTime.now());
    }
}
