package tea4life.user_service.controller.admin;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tea4life.user_service.dto.base.ApiResponse;
import tea4life.user_service.dto.base.PageResponse;
import tea4life.user_service.dto.request.UserRoleAssign;
import tea4life.user_service.dto.response.UserResponse;
import tea4life.user_service.dto.response.UserSummaryResponse;
import tea4life.user_service.service.AdminUserService;

/**
 * Admin 2/21/2026
 *
 **/
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasAuthority('ADMIN')")
public class UserAdminController {

    AdminUserService adminUserService;

    @GetMapping()
    public ApiResponse<PageResponse<UserSummaryResponse>> findAllUsers(
            @PageableDefault(value = 10) Pageable pageable
    ) {
        PageResponse<UserSummaryResponse> page = new PageResponse<>(
                adminUserService.findAllUsers(pageable)
        );

        return new ApiResponse<>(page);
    }

    @GetMapping("/{keycloakId}")
    public ApiResponse<UserResponse> findByKeycloakId(
            @PathVariable("keycloakId") String keycloakId
    ) {
        return new ApiResponse<>(adminUserService.findByKeycloakId(keycloakId));
    }

    @PostMapping("/assign-role")
    public ApiResponse<Void> assignRole(@RequestBody @Valid UserRoleAssign request) {
        adminUserService.assignRole(request);
        return ApiResponse.<Void>builder().build();
    }
}
