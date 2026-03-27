package ltphat.inventory.backend.iam.application;

import ltphat.inventory.backend.iam.application.dto.RoleDto;
import ltphat.inventory.backend.iam.application.dto.UserDetailDto;
import ltphat.inventory.backend.iam.application.dto.UserDto;
import ltphat.inventory.backend.iam.domain.model.Role;
import ltphat.inventory.backend.iam.domain.model.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AuthApplicationMapperTest {

    private final AuthApplicationMapper mapper = new AuthApplicationMapperImpl();

    @Test
    void toDto_shouldReturnNullWhenUserNull() {
        assertThat(mapper.toDto((User) null)).isNull();
    }

    @Test
    void toDto_shouldMapUserWithRoleName() {
        User user = User.builder()
                .id(1L)
                .fullName("Admin")
                .role(Role.builder().id(9L).name("ADMIN").build())
                .build();

        UserDto dto = mapper.toDto(user);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getFullName()).isEqualTo("Admin");
        assertThat(dto.getRole()).isEqualTo("ADMIN");
    }

    @Test
    void toDto_shouldMapNullRoleNameWhenUserRoleMissing() {
        User user = User.builder().id(2L).fullName("NoRole").role(null).build();

        UserDto dto = mapper.toDto(user);

        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getRole()).isNull();
    }

    @Test
    void toDetailDto_shouldReturnNullWhenUserNull() {
        assertThat(mapper.toDetailDto(null)).isNull();
    }

    @Test
    void toDetailDto_shouldMapNestedRole() {
        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .id(3L)
                .username("user3")
                .fullName("User Three")
                .email("u3@example.com")
                .role(Role.builder().id(5L).name("MANAGER").build())
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        UserDetailDto dto = mapper.toDetailDto(user);

        assertThat(dto.getId()).isEqualTo(3L);
        assertThat(dto.getRole()).isNotNull();
        assertThat(dto.getRole().getName()).isEqualTo("MANAGER");
    }

    @Test
    void toRoleDto_shouldHandleNullAndMap() {
        assertThat(mapper.toDto((Role) null)).isNull();

        RoleDto dto = mapper.toDto(Role.builder().id(7L).name("ADMIN").build());
        assertThat(dto.getId()).isEqualTo(7L);
        assertThat(dto.getName()).isEqualTo("ADMIN");
    }
}
