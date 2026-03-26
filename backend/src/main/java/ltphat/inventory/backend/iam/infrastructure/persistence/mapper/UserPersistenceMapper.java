package ltphat.inventory.backend.iam.infrastructure.persistence.mapper;

import ltphat.inventory.backend.iam.domain.model.Role;
import ltphat.inventory.backend.iam.domain.model.User;
import ltphat.inventory.backend.iam.infrastructure.persistence.entity.JpaRole;
import ltphat.inventory.backend.iam.infrastructure.persistence.entity.JpaUser;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserPersistenceMapper {
    User toDomain(JpaUser entity);
    JpaUser toEntity(User domain);
    
    Role toDomain(JpaRole entity);
    JpaRole toEntity(Role domain);
}
