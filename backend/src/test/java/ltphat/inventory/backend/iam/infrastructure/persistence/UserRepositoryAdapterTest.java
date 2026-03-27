package ltphat.inventory.backend.iam.infrastructure.persistence;

import ltphat.inventory.backend.iam.domain.model.User;
import ltphat.inventory.backend.iam.infrastructure.persistence.entity.JpaUser;
import ltphat.inventory.backend.iam.infrastructure.persistence.mapper.UserPersistenceMapper;
import ltphat.inventory.backend.iam.infrastructure.persistence.repository.SpringDataUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRepositoryAdapterTest {

    @Mock
    private SpringDataUserRepository springDataUserRepository;

    @Mock
    private UserPersistenceMapper mapper;

    @InjectMocks
    private UserRepositoryAdapter adapter;

    @Test
    void findByUsername_shouldMapResult() {
        JpaUser entity = JpaUser.builder().id(1L).username("user").build();
        User domain = User.builder().id(1L).username("user").build();

        when(springDataUserRepository.findByUsername("user")).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Optional<User> result = adapter.findByUsername("user");

        assertThat(result).contains(domain);
    }

    @Test
    void findByUsername_shouldReturnEmptyWhenNotFound() {
        when(springDataUserRepository.findByUsername("missing")).thenReturn(Optional.empty());

        Optional<User> result = adapter.findByUsername("missing");

        assertThat(result).isEmpty();
    }

    @Test
    void save_shouldMapAndPersist() {
        User domain = User.builder().id(2L).username("save-user").build();
        JpaUser entity = JpaUser.builder().id(2L).username("save-user").build();

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(springDataUserRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domain);

        User result = adapter.save(domain);

        assertThat(result).isEqualTo(domain);
    }

    @Test
    void findById_shouldMapResult() {
        JpaUser entity = JpaUser.builder().id(3L).username("id-user").build();
        User domain = User.builder().id(3L).username("id-user").build();

        when(springDataUserRepository.findById(3L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Optional<User> result = adapter.findById(3L);

        assertThat(result).contains(domain);
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        when(springDataUserRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<User> result = adapter.findById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void existsByUsername_shouldDelegate() {
        when(springDataUserRepository.existsByUsername("exists")).thenReturn(true);
        assertThat(adapter.existsByUsername("exists")).isTrue();
    }

    @Test
    void findAll_shouldHandleNullRoleAndNullIsActive() {
        Pageable pageable = PageRequest.of(0, 20);
        JpaUser entity = JpaUser.builder().id(10L).username("u1").build();
        User domain = User.builder().id(10L).username("u1").build();
        Page<JpaUser> page = new PageImpl<>(List.of(entity));

        when(springDataUserRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(mapper.toDomain(entity)).thenReturn(domain);

        Page<User> result = adapter.findAll(pageable, null, null);

        assertThat(result.getContent()).containsExactly(domain);
    }

    @Test
    void findAll_shouldHandleBlankRoleAndActiveFilter() {
        Pageable pageable = PageRequest.of(0, 20);
        JpaUser entity = JpaUser.builder().id(11L).username("u2").build();
        User domain = User.builder().id(11L).username("u2").build();
        Page<JpaUser> page = new PageImpl<>(List.of(entity));

        when(springDataUserRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(mapper.toDomain(entity)).thenReturn(domain);

        Page<User> result = adapter.findAll(pageable, "   ", true);

        assertThat(result.getContent()).containsExactly(domain);
    }

    @Test
    void findAll_shouldHandleRoleFilter() {
        Pageable pageable = PageRequest.of(1, 10);
        JpaUser entity = JpaUser.builder().id(12L).username("u3").build();
        User domain = User.builder().id(12L).username("u3").build();
        Page<JpaUser> page = new PageImpl<>(List.of(entity));

        when(springDataUserRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(mapper.toDomain(entity)).thenReturn(domain);

        Page<User> result = adapter.findAll(pageable, "ADMIN", null);

        assertThat(result.getContent()).containsExactly(domain);

        ArgumentCaptor<Specification<JpaUser>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(springDataUserRepository).findAll(specCaptor.capture(), eq(pageable));
        assertThat(specCaptor.getValue()).isNotNull();

        RootPredicateFixtures fixtures = new RootPredicateFixtures();
        Predicate predicate = specCaptor.getValue().toPredicate(fixtures.root, fixtures.query, fixtures.cb);
        assertThat(predicate).isSameAs(fixtures.combined);
    }

    @Test
    void existsByUsernameAndIdNot_shouldDelegate() {
        when(springDataUserRepository.existsByUsernameAndIdNot("user", 9L)).thenReturn(true);
        assertThat(adapter.existsByUsernameAndIdNot("user", 9L)).isTrue();
    }

    private static class RootPredicateFixtures {
        @SuppressWarnings("unchecked")
        final jakarta.persistence.criteria.Root<JpaUser> root = org.mockito.Mockito.mock(jakarta.persistence.criteria.Root.class);
        final CriteriaQuery<?> query = org.mockito.Mockito.mock(CriteriaQuery.class);
        final CriteriaBuilder cb = org.mockito.Mockito.mock(CriteriaBuilder.class);
        final Join<Object, Object> join = org.mockito.Mockito.mock(Join.class);
        final Path<Object> roleNamePath = org.mockito.Mockito.mock(Path.class);
        final Path<Object> activePath = org.mockito.Mockito.mock(Path.class);
        final Predicate conjunction = org.mockito.Mockito.mock(Predicate.class);
        final Predicate rolePredicate = org.mockito.Mockito.mock(Predicate.class);
        final Predicate activePredicate = org.mockito.Mockito.mock(Predicate.class);
        final Predicate combined = org.mockito.Mockito.mock(Predicate.class);

        RootPredicateFixtures() {
            when(cb.conjunction()).thenReturn(conjunction);
            lenient().when(root.join("role")).thenReturn(join);
            lenient().when(join.get("name")).thenReturn(roleNamePath);
            lenient().when(root.get("isActive")).thenReturn(activePath);
            lenient().when(cb.equal(roleNamePath, "ADMIN")).thenReturn(rolePredicate);
            lenient().when(cb.equal(activePath, true)).thenReturn(activePredicate);
            lenient().when(cb.and(any(Predicate.class), any(Predicate.class))).thenReturn(combined);
        }
    }
}
