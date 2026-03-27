package ltphat.inventory.backend.iam.infrastructure.persistence;

import ltphat.inventory.backend.iam.domain.model.RefreshToken;
import ltphat.inventory.backend.iam.domain.model.User;
import ltphat.inventory.backend.iam.infrastructure.persistence.entity.JpaRefreshToken;
import ltphat.inventory.backend.iam.infrastructure.persistence.entity.JpaUser;
import ltphat.inventory.backend.iam.infrastructure.persistence.mapper.RefreshTokenPersistenceMapper;
import ltphat.inventory.backend.iam.infrastructure.persistence.mapper.UserPersistenceMapper;
import ltphat.inventory.backend.iam.infrastructure.persistence.repository.SpringDataRefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenRepositoryAdapterTest {

    @Mock
    private SpringDataRefreshTokenRepository springDataRefreshTokenRepository;

    @Mock
    private RefreshTokenPersistenceMapper mapper;

    @Mock
    private UserPersistenceMapper userMapper;

    @InjectMocks
    private RefreshTokenRepositoryAdapter adapter;

    @Test
    void findByToken_shouldMapResult() {
        JpaRefreshToken entity = JpaRefreshToken.builder().id(1L).token("token").build();
        RefreshToken domain = RefreshToken.builder().id(1L).token("token").build();

        when(springDataRefreshTokenRepository.findByToken("token")).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Optional<RefreshToken> result = adapter.findByToken("token");

        assertThat(result).contains(domain);
    }

    @Test
    void save_shouldMapAndPersist() {
        RefreshToken domain = RefreshToken.builder().id(2L).token("token-2").build();
        JpaRefreshToken entity = JpaRefreshToken.builder().id(2L).token("token-2").build();

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(springDataRefreshTokenRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domain);

        RefreshToken result = adapter.save(domain);

        assertThat(result).isEqualTo(domain);
    }

    @Test
    void deleteByUser_shouldMapUserAndDelegate() {
        User user = User.builder().id(3L).username("u3").build();
        JpaUser entity = JpaUser.builder().id(3L).username("u3").build();

        when(userMapper.toEntity(user)).thenReturn(entity);

        adapter.deleteByUser(user);

        verify(springDataRefreshTokenRepository).deleteByUser(entity);
    }

    @Test
    void deleteByToken_shouldDelegate() {
        adapter.deleteByToken("token");
        verify(springDataRefreshTokenRepository).deleteByToken("token");
    }

    @Test
    void deleteByUserId_shouldDelegate() {
        adapter.deleteByUserId(9L);
        verify(springDataRefreshTokenRepository).deleteByUserId(9L);
    }
}
