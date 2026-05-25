package tea4life.user_service.config.database;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tea4life.user_service.model.Role;
import tea4life.user_service.model.constant.RoleName;
import tea4life.user_service.repository.RoleRepository;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RoleDataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        Map<String, String> roles = Map.of(
                RoleName.ADMIN, "Quản trị viên hệ thống",
                RoleName.MEMBER, "Người dùng mua hàng",
                RoleName.STORE, "Nhân viên bán hàng",
                RoleName.DRIVER, "Tài xế giao hàng"
        );

        roles.forEach((name, description) -> {
            if (!roleRepository.existsByName(name)) {
                roleRepository.save(Role.builder()
                        .name(name)
                        .description(description)
                        .build());
            }
        });
    }
}
