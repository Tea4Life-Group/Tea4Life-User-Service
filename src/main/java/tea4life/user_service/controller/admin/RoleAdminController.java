
package tea4life.user_service.controller.admin;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tea4life.user_service.dto.base.ApiResponse;
import tea4life.user_service.dto.base.PageResponse;
import tea4life.user_service.dto.request.UpsertRoleRequest;
import tea4life.user_service.dto.response.RoleResponse;
import tea4life.user_service.service.RoleService;

import java.util.List;

/**
 * Admin 2/16/2026
 *
 **/
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/admin/roles")
@PreAuthorize("hasAuthority('ADMIN')")
public class RoleAdminController {

    RoleService roleService;

    @PostMapping()
    public ApiResponse<@NonNull Void> createPermission(
            @RequestBody @Valid UpsertRoleRequest upsertRoleRequest
    ) {
        roleService.createRole(upsertRoleRequest);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping()
    public ApiResponse<PageResponse<RoleResponse>> findAllRoles(
            @PageableDefault(value = 10) Pageable pageable
    ) {
        PageResponse<RoleResponse> page = new PageResponse<>(
                roleService.findAllRoles(pageable)
        );

        return new ApiResponse<>(page);
    }

    @GetMapping("/all")
    public ApiResponse<List<RoleResponse>> findAllRoles() {
        return new ApiResponse<>(roleService.findAllRoles());
    }

    @GetMapping("/{id}")
    public ApiResponse<RoleResponse> findById(@PathVariable("id") Long id) {
        return new ApiResponse<>(roleService.findById(id));
    }

    @PostMapping("/{id}")
    public ApiResponse<@NonNull Void> updatePermission(
            @RequestBody @Valid UpsertRoleRequest upsertRoleRequest,
            @PathVariable("id") Long id
    ) {
        roleService.updateRole(upsertRoleRequest, id);
        return ApiResponse.<Void>builder().build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<@NonNull Void> deletePermission(
            @PathVariable("id") Long id
    ) {
        roleService.deleteRoleById(id);
        return ApiResponse.<Void>builder().build();
    }


}
