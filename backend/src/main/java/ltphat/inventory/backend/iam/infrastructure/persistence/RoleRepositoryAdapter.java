package ltphat.inventory.backend.iam.infrastructure.persistence;

import ltphat.inventory.backend.iam.domain.model.Role;
import ltphat.inventory.backend.iam.domain.repository.IRoleRepository;
import ltphat.inventory.backend.iam.infrastructure.persistence.mapper.UserPersistenceMapper;
import ltphat.inventory.backend.iam.infrastructure.persistence.repository.SpringDataRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RoleRepositoryAdapter implements IRoleRepository {

    private final SpringDataRoleRepository springDataRoleRepository;
    private final UserPersistenceMapper mapper;

    @Override
    public Optional<Role> findByName(String name) {
        return springDataRoleRepository.findByName(name)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Role> findById(Long id) {
        return springDataRoleRepository.findById(id)
                .map(mapper::toDomain);
    }
}
