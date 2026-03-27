package ltphat.inventory.backend.iam.application.service.impl;

import ltphat.inventory.backend.iam.application.AuthApplicationMapper;
import ltphat.inventory.backend.iam.application.dto.CreateUserRequest;
import ltphat.inventory.backend.iam.application.dto.ResetPasswordRequest;
import ltphat.inventory.backend.iam.application.dto.RoleDto;
import ltphat.inventory.backend.iam.application.dto.UpdateUserRequest;
import ltphat.inventory.backend.iam.application.dto.UserDetailDto;
import ltphat.inventory.backend.iam.domain.exception.DuplicateUsernameException;
import ltphat.inventory.backend.iam.domain.exception.RoleNotFoundException;
import ltphat.inventory.backend.iam.domain.exception.UserNotFoundException;
import ltphat.inventory.backend.iam.domain.model.Role;
import ltphat.inventory.backend.iam.domain.model.User;
import ltphat.inventory.backend.iam.domain.repository.IRoleRepository;
import ltphat.inventory.backend.iam.domain.repository.IUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserManagementAppServiceImplTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IRoleRepository roleRepository;

    @Mock
    private AuthApplicationMapper mapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserManagementAppServiceImpl service;

    @Test
    void listUsers_shouldConvertPaginationAndMapResult() {
        User user = User.builder().id(1L).username("u1").build();
        UserDetailDto dto = UserDetailDto.builder().id(1L).username("u1").build();
        Page<User> users = new PageImpl<>(List.of(user));

        when(userRepository.findAll(any(Pageable.class), any(), any())).thenReturn(users);
        when(mapper.toDetailDto(user)).thenReturn(dto);

        Page<UserDetailDto> result = service.listUsers(2, 20, null, "ADMIN", true);

        assertThat(result.getContent()).containsExactly(dto);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findAll(pageableCaptor.capture(), any(), any());
        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(20);
    }

    @Test
    void createUser_shouldThrowWhenUsernameExists() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("duplicate");

        when(userRepository.existsByUsername("duplicate")).thenReturn(true);

        assertThatThrownBy(() -> service.createUser(request))
                .isInstanceOf(DuplicateUsernameException.class)
                .hasMessage("Username already exists: duplicate");
    }

    @Test
    void createUser_shouldThrowWhenRoleMissing() {
        CreateUserRequest request = createUserRequest();
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(roleRepository.findById(request.getRoleId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createUser(request))
                .isInstanceOf(RoleNotFoundException.class)
                .hasMessage("Role not found with ID: 1");
    }

    @Test
    void createUser_shouldSaveActiveUserWithEncodedPassword() {
        CreateUserRequest request = createUserRequest();
        Role role = Role.builder().id(1L).name("ADMIN").build();
        User saved = User.builder()
                .id(5L)
                .username(request.getUsername())
                .passwordHash("encoded-pass")
                .fullName(request.getFullName())
                .email(request.getEmail())
                .role(role)
                .isActive(true)
                .build();
        UserDetailDto detailDto = UserDetailDto.builder()
                .id(5L)
                .username(request.getUsername())
                .fullName(request.getFullName())
                .role(RoleDto.builder().id(1L).name("ADMIN").build())
                .isActive(true)
                .build();

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("password123")).thenReturn("encoded-pass");
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(mapper.toDetailDto(saved)).thenReturn(detailDto);

        UserDetailDto result = service.createUser(request);

        assertThat(result).isEqualTo(detailDto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getIsActive()).isTrue();
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("encoded-pass");
    }

    @Test
    void getUser_shouldReturnDtoWhenFound() {
        User user = User.builder().id(1L).username("u1").build();
        UserDetailDto dto = UserDetailDto.builder().id(1L).username("u1").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(mapper.toDetailDto(user)).thenReturn(dto);

        UserDetailDto result = service.getUser(1L);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    void getUser_shouldThrowWhenMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUser(1L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with ID: 1");
    }

    @Test
    void updateUser_shouldThrowWhenUserMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateUser(99L, updateUserRequest(null)))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with ID: 99");
    }

    @Test
    void updateUser_shouldThrowWhenRoleMissing() {
        User existing = User.builder().id(1L).username("user").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(roleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateUser(1L, updateUserRequest("new-password")))
                .isInstanceOf(RoleNotFoundException.class)
                .hasMessage("Role not found with ID: 1");
    }

    @Test
    void updateUser_shouldUpdateWithoutChangingPasswordWhenPasswordNull() {
        Role role = Role.builder().id(1L).name("ADMIN").build();
        User existing = User.builder()
                .id(1L)
                .username("user")
                .passwordHash("old-hash")
                .role(role)
                .build();
        User saved = User.builder()
                .id(1L)
                .username("user")
                .passwordHash("old-hash")
                .fullName("Updated Name")
                .email("updated@example.com")
                .role(role)
                .build();
        UserDetailDto dto = UserDetailDto.builder().id(1L).username("user").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(userRepository.save(existing)).thenReturn(saved);
        when(mapper.toDetailDto(saved)).thenReturn(dto);

        UserDetailDto result = service.updateUser(1L, updateUserRequest(null));

        assertThat(result).isEqualTo(dto);
        assertThat(existing.getPasswordHash()).isEqualTo("old-hash");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void updateUser_shouldUpdatePasswordWhenProvided() {
        Role role = Role.builder().id(1L).name("ADMIN").build();
        User existing = User.builder()
                .id(1L)
                .username("user")
                .passwordHash("old-hash")
                .role(role)
                .build();
        User saved = User.builder()
                .id(1L)
                .username("user")
                .passwordHash("encoded-new")
                .fullName("Updated Name")
                .email("updated@example.com")
                .role(role)
                .build();
        UserDetailDto dto = UserDetailDto.builder().id(1L).username("user").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-new");
        when(userRepository.save(existing)).thenReturn(saved);
        when(mapper.toDetailDto(saved)).thenReturn(dto);

        UserDetailDto result = service.updateUser(1L, updateUserRequest("new-password"));

        assertThat(result).isEqualTo(dto);
        assertThat(existing.getPasswordHash()).isEqualTo("encoded-new");
    }

    @Test
    void deactivateUser_shouldSetInactiveAndSave() {
        User user = User.builder().id(2L).isActive(true).build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        service.deactivateUser(2L);

        assertThat(user.getIsActive()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    void deactivateUser_shouldThrowWhenMissing() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deactivateUser(2L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with ID: 2");
    }

    @Test
    void resetPassword_shouldEncodeAndSave() {
        User user = User.builder().id(3L).passwordHash("old").build();
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setNewPassword("new-password");

        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-new");

        service.resetPassword(3L, request);

        assertThat(user.getPasswordHash()).isEqualTo("encoded-new");
        verify(userRepository).save(user);
    }

    @Test
    void resetPassword_shouldThrowWhenMissing() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setNewPassword("new-password");
        when(userRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resetPassword(3L, request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with ID: 3");
    }

    private CreateUserRequest createUserRequest() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("new_user");
        request.setPassword("password123");
        request.setFullName("New User");
        request.setEmail("new@example.com");
        request.setRoleId(1L);
        return request;
    }

    private UpdateUserRequest updateUserRequest(String password) {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFullName("Updated Name");
        request.setEmail("updated@example.com");
        request.setRoleId(1L);
        request.setPassword(password);
        return request;
    }
}
