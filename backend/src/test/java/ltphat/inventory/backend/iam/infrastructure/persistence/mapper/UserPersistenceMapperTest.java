package ltphat.inventory.backend.iam.infrastructure.persistence.mapper;

import ltphat.inventory.backend.iam.domain.model.Role;
import ltphat.inventory.backend.iam.domain.model.User;
import ltphat.inventory.backend.iam.infrastructure.persistence.entity.JpaRole;
import ltphat.inventory.backend.iam.infrastructure.persistence.entity.JpaUser;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserPersistenceMapperTest {

    private final UserPersistenceMapper mapper = new UserPersistenceMapperImpl();

    @Test
    void toDomain_shouldReturnNullForNullInput() {
        assertThat(mapper.toDomain((JpaUser) null)).isNull();
        assertThat(mapper.toDomain((JpaRole) null)).isNull();
    }

    @Test
    void toEntity_shouldReturnNullForNullInput() {
        assertThat(mapper.toEntity((User) null)).isNull();
        assertThat(mapper.toEntity((Role) null)).isNull();
    }

    @Test
    void toDomain_shouldMapUserAndRole() {
        LocalDateTime now = LocalDateTime.now();
        JpaRole jpaRole = JpaRole.builder().id(1L).name("ADMIN").description("Admin role").createdAt(now).build();
        JpaUser jpaUser = JpaUser.builder()
                .id(9L)
                .username("user")
                .passwordHash("hash")
                .fullName("User")
                .email("u@example.com")
                .role(jpaRole)
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        User domain = mapper.toDomain(jpaUser);

        assertThat(domain.getId()).isEqualTo(9L);
        assertThat(domain.getRole()).isNotNull();
        assertThat(domain.getRole().getName()).isEqualTo("ADMIN");
    }

    @Test
    void toEntity_shouldMapUserAndRole() {
        LocalDateTime now = LocalDateTime.now();
        Role role = Role.builder().id(2L).name("MANAGER").description("Mgr").createdAt(now).build();
        User user = User.builder()
                .id(10L)
                .username("domain-user")
                .passwordHash("hash")
                .fullName("Domain")
                .email("d@example.com")
                .role(role)
                .isActive(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        JpaUser entity = mapper.toEntity(user);

        assertThat(entity.getId()).isEqualTo(10L);
        assertThat(entity.getRole()).isNotNull();
        assertThat(entity.getRole().getName()).isEqualTo("MANAGER");
        assertThat(entity.getIsActive()).isFalse();
    }
}
