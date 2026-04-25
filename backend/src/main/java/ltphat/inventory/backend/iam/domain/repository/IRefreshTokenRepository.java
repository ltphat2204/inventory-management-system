package ltphat.inventory.backend.iam.domain.repository;

import ltphat.inventory.backend.iam.domain.model.RefreshToken;
import ltphat.inventory.backend.iam.domain.model.User;
import java.util.Optional;

public interface IRefreshTokenRepository {
    Optional<RefreshToken> findByToken(String token);
    RefreshToken save(RefreshToken refreshToken);
    void deleteByUser(User user);
    void deleteByToken(String token);
    void deleteByUserId(Long userId);
    void flush();
}
