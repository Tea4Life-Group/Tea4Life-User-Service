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
import tea4life.user_service.dto.request.UpsertPermissionRequest;
import tea4life.user_service.dto.response.PermissionResponse;
import tea4life.user_service.service.PermissionService;

import java.util.List;

/**
 * Admin 2/16/2026
 *
 **/
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/admin/permissions")
@PreAuthorize("hasAuthority('ADMIN')")
public class PermissionAdminController {

    PermissionService permissionService;

    @PostMapping()
    public ApiResponse<@NonNull Void> createPermission(
            @RequestBody @Valid UpsertPermissionRequest upsertPermissionRequest
    ) {
        permissionService.createPermission(upsertPermissionRequest);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping()
    public ApiResponse<PageResponse<PermissionResponse>> findAllPermissions(
            @PageableDefault(value = 10) Pageable pageable
    ) {
        PageResponse<PermissionResponse> page = new PageResponse<>(
                permissionService.findAllPermissions(pageable)
        );

        return new ApiResponse<>(page);
    }

    @GetMapping("/all")
    public ApiResponse<List<PermissionResponse>> findAllPermissions() {
        return new ApiResponse<>(permissionService.findAllPermissions());
    }

    @PostMapping("/{id}")
    public ApiResponse<@NonNull Void> updatePermission(
            @RequestBody @Valid UpsertPermissionRequest upsertPermissionRequest,
            @PathVariable("id") Long id
    ) {
        permissionService.updatePermission(upsertPermissionRequest, id);
        return ApiResponse.<Void>builder().build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<@NonNull Void> deletePermission(
            @PathVariable("id") Long id
    ) {
        permissionService.deletePermissionById(id);
        return ApiResponse.<Void>builder().build();
    }


}
