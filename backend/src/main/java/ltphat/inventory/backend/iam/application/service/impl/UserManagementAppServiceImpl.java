package ltphat.inventory.backend.iam.application.service.impl;

import lombok.RequiredArgsConstructor;
import ltphat.inventory.backend.iam.application.AuthApplicationMapper;
import ltphat.inventory.backend.iam.application.dto.*;
import ltphat.inventory.backend.iam.application.service.IUserManagementAppService;
import ltphat.inventory.backend.iam.domain.command.CreateUserCommand;
import ltphat.inventory.backend.iam.domain.command.UpdateUserCommand;
import ltphat.inventory.backend.iam.domain.exception.DuplicateUsernameException;
import ltphat.inventory.backend.iam.domain.exception.RoleNotFoundException;
import ltphat.inventory.backend.iam.domain.exception.UserNotFoundException;
import ltphat.inventory.backend.iam.domain.model.Role;
import ltphat.inventory.backend.iam.domain.model.User;
import ltphat.inventory.backend.iam.domain.repository.IRoleRepository;
import ltphat.inventory.backend.iam.domain.repository.IUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserManagementAppServiceImpl implements IUserManagementAppService {

    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final AuthApplicationMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<UserDetailDto> listUsers(int page, int limit, String sort, String role, Boolean isActive) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        return userRepository.findAll(pageable, role, isActive)
                .map(mapper::toDetailDto);
    }

    @Override
    @Transactional
    public UserDetailDto createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUsernameException("Username already exists: " + request.getUsername());
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RoleNotFoundException("Role not found with ID: " + request.getRoleId()));

        CreateUserCommand command = CreateUserCommand.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .role(role)
                .build();

        User user = User.builder()
                .username(command.getUsername())
                .passwordHash(command.getPasswordHash())
                .fullName(command.getFullName())
                .email(command.getEmail())
                .role(command.getRole())
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        return mapper.toDetailDto(savedUser);
    }

    @Override
    public UserDetailDto getUser(Long id) {
        return userRepository.findById(id)
                .map(mapper::toDetailDto)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
    }

    @Override
    @Transactional
    public UserDetailDto updateUser(Long id, UpdateUserRequest request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RoleNotFoundException("Role not found with ID: " + request.getRoleId()));

        UpdateUserCommand command = UpdateUserCommand.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .role(role)
                .passwordHash(request.getPassword() != null ? passwordEncoder.encode(request.getPassword()) : null)
                .build();

        existingUser.setFullName(command.getFullName());
        existingUser.setEmail(command.getEmail());
        existingUser.setRole(command.getRole());
        
        if (command.getPasswordHash() != null) {
            existingUser.setPasswordHash(command.getPasswordHash());
        }

        User savedUser = userRepository.save(existingUser);
        return mapper.toDetailDto(savedUser);
    }

    @Override
    @Transactional
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetPassword(Long id, ResetPasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
