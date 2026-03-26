package ltphat.inventory.backend.iam.infrastructure.persistence.mapper;

import ltphat.inventory.backend.iam.domain.model.RefreshToken;
import ltphat.inventory.backend.iam.infrastructure.persistence.entity.JpaRefreshToken;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserPersistenceMapper.class})
public interface RefreshTokenPersistenceMapper {
    RefreshToken toDomain(JpaRefreshToken entity);

    @org.mapstruct.Mapping(target = "createdAt", ignore = true)
    JpaRefreshToken toEntity(RefreshToken domain);
}
