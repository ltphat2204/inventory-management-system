package ltphat.inventory.backend.iam.infrastructure.persistence.mapper;

import ltphat.inventory.backend.iam.domain.model.RefreshToken;
import ltphat.inventory.backend.iam.domain.model.Role;
import ltphat.inventory.backend.iam.domain.model.User;
import ltphat.inventory.backend.iam.infrastructure.persistence.entity.JpaRefreshToken;
import ltphat.inventory.backend.iam.infrastructure.persistence.entity.JpaRole;
import ltphat.inventory.backend.iam.infrastructure.persistence.entity.JpaUser;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenPersistenceMapperTest {

    private final UserPersistenceMapper userMapper = new UserPersistenceMapperImpl();
    private final RefreshTokenPersistenceMapperImpl mapper = new RefreshTokenPersistenceMapperImpl();

    RefreshTokenPersistenceMapperTest() {
        ReflectionTestUtils.setField(mapper, "userPersistenceMapper", userMapper);
    }

    @Test
    void toDomainAndToEntity_shouldReturnNullForNullInput() {
        assertThat(mapper.toDomain(null)).isNull();
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    void toDomain_shouldMapAllFields() {
        LocalDateTime now = LocalDateTime.now();
        JpaRole role = JpaRole.builder().id(1L).name("ADMIN").build();
        JpaUser user = JpaUser.builder().id(2L).username("u1").role(role).passwordHash("h").build();
        JpaRefreshToken entity = JpaRefreshToken.builder()
                .id(3L)
                .user(user)
                .token("token")
                .expiryDate(now.plusMinutes(5))
                .deviceId("dev-1")
                .lastIp("10.0.0.1")
                .lastUserAgent("JUnit")
                .lastUsedAt(now)
                .createdAt(now)
                .build();

        RefreshToken domain = mapper.toDomain(entity);

        assertThat(domain.getId()).isEqualTo(3L);
        assertThat(domain.getUser()).isNotNull();
        assertThat(domain.getToken()).isEqualTo("token");
        assertThat(domain.getLastIp()).isEqualTo("10.0.0.1");
    }

    @Test
    void toEntity_shouldMapAllFieldsAndIgnoreCreatedAt() {
        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .id(4L)
                .username("u2")
                .passwordHash("h")
                .role(Role.builder().id(9L).name("MANAGER").build())
                .build();
        RefreshToken domain = RefreshToken.builder()
                .id(5L)
                .user(user)
                .token("token-2")
                .expiryDate(now.plusMinutes(10))
                .deviceId("dev-2")
                .lastIp("10.0.0.2")
                .lastUserAgent("Agent")
                .lastUsedAt(now)
                .build();

        JpaRefreshToken entity = mapper.toEntity(domain);

        assertThat(entity.getId()).isEqualTo(5L);
        assertThat(entity.getUser()).isNotNull();
        assertThat(entity.getToken()).isEqualTo("token-2");
        assertThat(entity.getCreatedAt()).isNull();
    }
}
