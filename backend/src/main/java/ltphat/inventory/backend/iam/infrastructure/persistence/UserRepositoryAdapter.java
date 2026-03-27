package ltphat.inventory.backend.iam.infrastructure.persistence;

import ltphat.inventory.backend.iam.domain.model.User;
import ltphat.inventory.backend.iam.domain.repository.IUserRepository;
import ltphat.inventory.backend.iam.infrastructure.persistence.entity.JpaUser;
import ltphat.inventory.backend.iam.infrastructure.persistence.mapper.UserPersistenceMapper;
import ltphat.inventory.backend.iam.infrastructure.persistence.repository.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jakarta.persistence.criteria.Join;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements IUserRepository {

    private final SpringDataUserRepository springDataUserRepository;
    private final UserPersistenceMapper mapper;

    @Override
    public Optional<User> findByUsername(String username) {
        return springDataUserRepository.findByUsername(username)
                .map(mapper::toDomain);
    }

    @Override
    public User save(User user) {
        JpaUser entity = mapper.toEntity(user);
        JpaUser savedEntity = springDataUserRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(Long id) {
        return springDataUserRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return springDataUserRepository.existsByUsername(username);
    }

    @Override
    public Page<User> findAll(Pageable pageable, String roleName, Boolean isActive) {
        Specification<JpaUser> spec = (root, query, cb) -> cb.conjunction();

        if (roleName != null && !roleName.isBlank()) {
            spec = spec.and((root, query, cb) -> {
                Join<Object, Object> roleJoin = root.join("role");
                return cb.equal(roleJoin.get("name"), roleName);
            });
        }

        if (isActive != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isActive"), isActive));
        }

        return springDataUserRepository.findAll(spec, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByUsernameAndIdNot(String username, Long id) {
        return springDataUserRepository.existsByUsernameAndIdNot(username, id);
    }
}
