package ltphat.inventory.backend.iam.presentation.controller;

import ltphat.inventory.backend.iam.application.dto.CreateUserRequest;
import ltphat.inventory.backend.iam.application.dto.ResetPasswordRequest;
import ltphat.inventory.backend.iam.application.dto.RoleDto;
import ltphat.inventory.backend.iam.application.dto.UpdateUserRequest;
import ltphat.inventory.backend.iam.application.dto.UserDetailDto;
import ltphat.inventory.backend.iam.application.service.IUserManagementAppService;
import ltphat.inventory.backend.iam.domain.exception.DuplicateUsernameException;
import ltphat.inventory.backend.iam.domain.exception.UserNotFoundException;
import ltphat.inventory.backend.shared.api.exception.GlobalExceptionHandler;
import ltphat.inventory.backend.shared.api.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private IUserManagementAppService userManagementAppService;

    @InjectMocks
    private UserController userController;

    @Test
    void createUser_shouldReturn201ForAdminFlow() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("new_user");
        request.setPassword("password123");
        request.setFullName("New User");
        request.setEmail("new@example.com");
        request.setRoleId(1L);

        UserDetailDto user = UserDetailDto.builder()
                .id(10L)
                .username("new_user")
                .fullName("New User")
                .role(RoleDto.builder().id(1L).name("MANAGER").build())
                .isActive(true)
                .build();

        when(userManagementAppService.createUser(request)).thenReturn(user);

        ResponseEntity<ApiResponse<UserDetailDto>> response = userController.createUser(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData().getUsername()).isEqualTo("new_user");
        assertThat(response.getBody().getData().getIsActive()).isTrue();
    }

    @Test
    void createUser_shouldMapDuplicateUsernameTo400ViaHandler() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("duplicate_user");

        when(userManagementAppService.createUser(any(CreateUserRequest.class)))
                .thenThrow(new DuplicateUsernameException("Username already exists: duplicate_user"));

        assertThatThrownBy(() -> userController.createUser(request))
                .isInstanceOf(DuplicateUsernameException.class);

        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ResponseEntity<ApiResponse<Void>> errorResponse =
                handler.handleDuplicateUsernameException(new DuplicateUsernameException("Username already exists: duplicate_user"));

        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorResponse.getBody()).isNotNull();
        assertThat(errorResponse.getBody().getErrorCode()).isEqualTo("DUPLICATE_USERNAME");
    }

    @Test
    void createUser_shouldDeclareAdminOnlyAuthorization() throws Exception {
        Method method = UserController.class.getMethod("createUser", CreateUserRequest.class);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasRole('ADMIN')");
    }

    @Test
    void resetPassword_shouldReturn200ForAdminFlow() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setNewPassword("new-password");

        doNothing().when(userManagementAppService).resetPassword(eq(10L), eq(request));

        ResponseEntity<ApiResponse<Void>> response = userController.resetPassword(10L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
    }

    @Test
    void deactivateUser_shouldReturn200ForExistingUser() {
        doNothing().when(userManagementAppService).deactivateUser(10L);

        ResponseEntity<ApiResponse<Void>> response = userController.deactivateUser(10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
    }

    @Test
    void deactivateUser_shouldMapMissingUserTo404ViaHandler() {
        doThrow(new UserNotFoundException("User not found with ID: 99999"))
                .when(userManagementAppService).deactivateUser(99999L);

        assertThatThrownBy(() -> userController.deactivateUser(99999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with ID: 99999");

        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ResponseEntity<ApiResponse<Void>> errorResponse =
                handler.handleUserNotFoundException(new UserNotFoundException("User not found with ID: 99999"));

        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(errorResponse.getBody()).isNotNull();
        assertThat(errorResponse.getBody().getErrorCode()).isEqualTo("USER_NOT_FOUND");
    }

    @Test
    void listUsers_shouldReturn200ForAdminFlow() {
        UserDetailDto user = UserDetailDto.builder()
                .id(1L)
                .username("user1")
                .role(RoleDto.builder().id(1L).name("ADMIN").build())
                .isActive(true)
                .build();
        Page<UserDetailDto> page = new PageImpl<>(List.of(user));

        when(userManagementAppService.listUsers(1, 20, null, null, null)).thenReturn(page);

        ResponseEntity<ApiResponse<Page<UserDetailDto>>> response = userController.listUsers(1, 20, null, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData().getContent()).hasSize(1);
        assertThat(response.getBody().getData().getContent().getFirst().getUsername()).isEqualTo("user1");
    }

    @Test
    void getUser_shouldReturn200() {
        UserDetailDto user = UserDetailDto.builder().id(2L).username("user2").build();
        when(userManagementAppService.getUser(2L)).thenReturn(user);

        ResponseEntity<ApiResponse<UserDetailDto>> response = userController.getUser(2L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getUsername()).isEqualTo("user2");
    }

    @Test
    void updateUser_shouldReturn200() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFullName("Updated");
        request.setEmail("updated@example.com");
        request.setRoleId(1L);

        UserDetailDto user = UserDetailDto.builder().id(3L).username("user3").fullName("Updated").build();
        when(userManagementAppService.updateUser(3L, request)).thenReturn(user);

        ResponseEntity<ApiResponse<UserDetailDto>> response = userController.updateUser(3L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getFullName()).isEqualTo("Updated");
    }
}
