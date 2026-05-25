package tea4life.user_service.dto.response;

import lombok.Builder;

import java.util.Set;

/**
 * Admin 2/20/2026
 *
 **/
@Builder
public record UserPermissionsResponse(
        String email,
        String role,
        Set<String> permissions
) {
}
