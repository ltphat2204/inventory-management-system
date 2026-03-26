package ltphat.inventory.backend.iam.application;

import ltphat.inventory.backend.iam.application.dto.UserDto;
import ltphat.inventory.backend.iam.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthApplicationMapper {
    @Mapping(source = "role.name", target = "role")
    UserDto toDto(User user);
}
