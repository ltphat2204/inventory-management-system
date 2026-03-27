package ltphat.inventory.backend.iam.infrastructure.persistence;

import ltphat.inventory.backend.iam.domain.model.Role;
import ltphat.inventory.backend.iam.infrastructure.persistence.entity.JpaRole;
import ltphat.inventory.backend.iam.infrastructure.persistence.mapper.UserPersistenceMapper;
import ltphat.inventory.backend.iam.infrastructure.persistence.repository.SpringDataRoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleRepositoryAdapterTest {

    @Mock
    private SpringDataRoleRepository springDataRoleRepository;

    @Mock
    private UserPersistenceMapper mapper;

    @InjectMocks
    private RoleRepositoryAdapter adapter;

    @Test
    void findByName_shouldMapResult() {
        JpaRole jpaRole = JpaRole.builder().id(1L).name("ADMIN").build();
        Role role = Role.builder().id(1L).name("ADMIN").build();

        when(springDataRoleRepository.findByName("ADMIN")).thenReturn(Optional.of(jpaRole));
        when(mapper.toDomain(jpaRole)).thenReturn(role);

        Optional<Role> result = adapter.findByName("ADMIN");

        assertThat(result).contains(role);
    }

    @Test
    void findById_shouldMapResult() {
        JpaRole jpaRole = JpaRole.builder().id(2L).name("MANAGER").build();
        Role role = Role.builder().id(2L).name("MANAGER").build();

        when(springDataRoleRepository.findById(2L)).thenReturn(Optional.of(jpaRole));
        when(mapper.toDomain(jpaRole)).thenReturn(role);

        Optional<Role> result = adapter.findById(2L);

        assertThat(result).contains(role);
    }
}
