package tea4life.user_service.service;

import org.springframework.transaction.annotation.Transactional;
import tea4life.user_service.dto.request.OnboardingRequest;
import tea4life.user_service.dto.request.UpdateAvatarRequest;
import tea4life.user_service.dto.request.UpdatePasswordRequest;
import tea4life.user_service.dto.request.UpdateProfileRequest;
import tea4life.user_service.dto.response.UserPermissionsResponse;
import tea4life.user_service.dto.response.UserProfileResponse;

/**
 * Admin 2/8/2026
 *
 **/
public interface UserService {
    void processOnboarding(OnboardingRequest onboardingRequest);

    void processOnboarding(Long userId, OnboardingRequest onboardingRequest);

    UserProfileResponse getUserProfile();

    void updateUserProfile(UpdateProfileRequest request);

    void updateUserAvatar(UpdateAvatarRequest request);

    void updateUserPassword(UpdatePasswordRequest request);

    @Transactional(readOnly = true)
    UserPermissionsResponse getUserPermissions(String keycloakId);

    void assignRoleByName(String keycloakId, String roleName);
}
