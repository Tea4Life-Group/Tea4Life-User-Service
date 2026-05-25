package tea4life.user_service.controller.admin;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tea4life.user_service.dto.base.ApiResponse;
import tea4life.user_service.dto.request.CreateAddressRequest;
import tea4life.user_service.dto.response.AddressResponse;
import tea4life.user_service.service.AdminAddressService;

import java.util.List;

/**
 * Admin 2/24/2026
 *
 **/
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/admin/users/{keycloakId}/addresses")
@PreAuthorize("hasAuthority('ADMIN')")
public class AddressAdminController {

    AdminAddressService adminAddressService;

    @PostMapping()
    public ApiResponse<AddressResponse> createAddress(
            @PathVariable("keycloakId") String keycloakId,
            @RequestBody @Valid CreateAddressRequest request
    ) {
        return new ApiResponse<>(adminAddressService.createAddress(keycloakId, request));
    }

    @GetMapping()
    public ApiResponse<List<AddressResponse>> findAddressesByKeycloakId(
            @PathVariable("keycloakId") String keycloakId
    ) {
        return new ApiResponse<>(adminAddressService.findAddressesByKeycloakId(keycloakId));
    }

    @GetMapping("/{id}")
    public ApiResponse<AddressResponse> findAddressById(
            @PathVariable("keycloakId") String keycloakId,
            @PathVariable("id") Long id
    ) {
        return new ApiResponse<>(adminAddressService.findAddressByKeycloakIdAndAddressId(keycloakId, id));
    }

    @PostMapping("/{id}")
    public ApiResponse<AddressResponse> updateAddress(
            @PathVariable("keycloakId") String keycloakId,
            @PathVariable("id") Long id,
            @RequestBody @Valid CreateAddressRequest request
    ) {
        return new ApiResponse<>(adminAddressService.updateAddressByKeycloakId(keycloakId, id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<@NonNull Void> deleteAddress(
            @PathVariable("keycloakId") String keycloakId,
            @PathVariable("id") Long id
    ) {
        adminAddressService.deleteAddressByKeycloakId(keycloakId, id);
        return ApiResponse.<Void>builder().build();
    }
}
