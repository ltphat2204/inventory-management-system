package ltphat.inventory.backend.iam.infrastructure.persistence.repository;

import ltphat.inventory.backend.iam.infrastructure.persistence.entity.JpaRefreshToken;
import ltphat.inventory.backend.iam.infrastructure.persistence.entity.JpaUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataRefreshTokenRepository extends JpaRepository<JpaRefreshToken, Long> {
    Optional<JpaRefreshToken> findByToken(String token);
    void deleteByUser(JpaUser user);
    void deleteByToken(String token);
}
