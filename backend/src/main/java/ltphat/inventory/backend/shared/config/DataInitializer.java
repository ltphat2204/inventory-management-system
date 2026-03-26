package ltphat.inventory.backend.shared.config;

import ltphat.inventory.backend.iam.infrastructure.persistence.entity.JpaRole;
import ltphat.inventory.backend.iam.infrastructure.persistence.entity.JpaUser;
import ltphat.inventory.backend.iam.infrastructure.persistence.repository.SpringDataRoleRepository;
import ltphat.inventory.backend.iam.infrastructure.persistence.repository.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SpringDataRoleRepository roleRepository;
    private final SpringDataUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        List<String> validRoles = Arrays.asList("ADMIN", "MANAGER", "CASHIER", "VIEWER", "SYSTEM_ADMIN");

        validRoles.forEach(roleName -> {
            if (roleRepository.findByName(roleName).isEmpty()) {
                JpaRole role = JpaRole.builder()
                        .name(roleName)
                        .description("Default " + roleName + " role")
                        .createdAt(LocalDateTime.now())
                        .build();
                roleRepository.save(role);
            }
        });

        if (!userRepository.existsByUsername("admin")) {
            JpaRole adminRole = roleRepository.findByName("ADMIN").orElseThrow();
            JpaUser adminUser = JpaUser.builder()
                    .username("admin")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .fullName("System Administrator")
                    .email("admin@inventory.local")
                    .role(adminRole)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userRepository.save(adminUser);
        }
    }
}
