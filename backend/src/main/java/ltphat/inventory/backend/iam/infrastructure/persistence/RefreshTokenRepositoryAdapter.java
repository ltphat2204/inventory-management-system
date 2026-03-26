package ltphat.inventory.backend.iam.infrastructure.persistence;

import ltphat.inventory.backend.iam.domain.model.RefreshToken;
import ltphat.inventory.backend.iam.domain.model.User;
import ltphat.inventory.backend.iam.domain.repository.IRefreshTokenRepository;
import ltphat.inventory.backend.iam.infrastructure.persistence.entity.JpaRefreshToken;
import ltphat.inventory.backend.iam.infrastructure.persistence.mapper.RefreshTokenPersistenceMapper;
import ltphat.inventory.backend.iam.infrastructure.persistence.mapper.UserPersistenceMapper;
import ltphat.inventory.backend.iam.infrastructure.persistence.repository.SpringDataRefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements IRefreshTokenRepository {

    private final SpringDataRefreshTokenRepository springDataRefreshTokenRepository;
    private final RefreshTokenPersistenceMapper mapper;
    private final UserPersistenceMapper userMapper;

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return springDataRefreshTokenRepository.findByToken(token)
                .map(mapper::toDomain);
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        JpaRefreshToken entity = mapper.toEntity(refreshToken);
        JpaRefreshToken savedEntity = springDataRefreshTokenRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    @Transactional
    public void deleteByUser(User user) {
        springDataRefreshTokenRepository.deleteByUser(userMapper.toEntity(user));
    }

    @Override
    @Transactional
    public void deleteByToken(String token) {
        springDataRefreshTokenRepository.deleteByToken(token);
    }
}
