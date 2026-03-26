package ltphat.inventory.backend.iam.application.service;

import ltphat.inventory.backend.iam.application.dto.*;
import org.springframework.data.domain.Page;

public interface IUserManagementAppService {
    Page<UserDetailDto> listUsers(int page, int limit, String sort, String role, Boolean isActive);
    UserDetailDto createUser(CreateUserRequest request);
    UserDetailDto getUser(Long id);
    UserDetailDto updateUser(Long id, UpdateUserRequest request);
    void deactivateUser(Long id);
    void resetPassword(Long id, ResetPasswordRequest request);
}
