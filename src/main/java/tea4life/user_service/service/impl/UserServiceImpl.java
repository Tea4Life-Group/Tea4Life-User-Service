package tea4life.user_service.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.BadRequestException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tea4life.user_service.client.StorageClient;
import tea4life.user_service.context.UserContext;
import tea4life.user_service.dto.base.ApiResponse;
import tea4life.user_service.dto.request.*;
import tea4life.user_service.dto.response.UserPermissionsResponse;
import tea4life.user_service.dto.response.UserProfileResponse;
import tea4life.user_service.model.Permission;
import tea4life.user_service.model.Role;
import tea4life.user_service.model.User;
import tea4life.user_service.repository.RoleRepository;
import tea4life.user_service.repository.UserRepository;
import tea4life.user_service.service.UserService;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Admin 2/8/2026
 *
 **/
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    StorageClient storageClient;
    Keycloak keycloak;
    RoleRepository roleRepository;

    KafkaTemplate<@NonNull String, @NonNull String> kafkaTemplate;

    @Value("${keycloak.server-url}")
    @NonFinal
    String serverUrl;

    @Value("${keycloak.current-realm}")
    @NonFinal
    String currentRealm;

    @Value("${keycloak.client-id}")
    @NonFinal
    String clientId;

    @Value("${spring.kafka.topic.storage-delete-file}")
    @NonFinal
    String storageDeleteFileTopic;

    @Override
    public void processOnboarding(OnboardingRequest onboardingRequest) {
        String email = UserContext.get().getEmail();
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng"));

        applyOnboarding(user, onboardingRequest);
    }

    @Override
    public void processOnboarding(Long userId, OnboardingRequest onboardingRequest) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng"));

        applyOnboarding(user, onboardingRequest);
    }

    private void applyOnboarding(User user, OnboardingRequest onboardingRequest) {
        try {
            if (onboardingRequest.avatarKey() != null && !onboardingRequest.avatarKey().isBlank()) {
                String destinationPath = "users/avatars/" + user.getId();
                ApiResponse<String> storageResponse = storageClient.confirmFile(
                        new FileMoveRequest(
                                onboardingRequest.avatarKey(),
                                destinationPath
                        )
                );

                if (storageResponse.getErrorCode() != null)
                    throw new RuntimeException("Lỗi di chuyển file: " + storageResponse.getErrorMessage());
                user.setAvatarUrl(storageResponse.getData());
            }


            user.setFullName(onboardingRequest.fullName());
            user.setPhone(onboardingRequest.phone());
            user.setDob(onboardingRequest.dob());
            user.setGender(onboardingRequest.gender());
            user.setOnBoarded(true);


            userRepository.save(user);
            log.info("Onboarding thành công cho user: {}", user.getId());

        } catch (Exception e) {
            log.error("Onboarding thất bại cho user {}: {}", user.getId(), e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile() {
        String email = UserContext.get().getEmail();
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng"));

        return UserProfileResponse
                .builder()
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .dob(user.getDob())
                .gender(user.getGender())
                .avatarUrl(user.getAvatarUrl())
                .id(user.getId().toString())
                .build();
    }

    @Override
    public void updateUserProfile(UpdateProfileRequest request) {
        String email = UserContext.get().getEmail();
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng"));

        user.setFullName(request.fullName());
        user.setPhone(request.phone());
        user.setDob(request.dob());
        user.setGender(request.gender());
    }

    @Override
    public void updateUserAvatar(UpdateAvatarRequest request) {
        String email = UserContext.get().getEmail();
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng"));

        if (request.avatarKey() != null && !request.avatarKey().isBlank()) {
            String oldAvatarUrl = user.getAvatarUrl();
            String destinationPath = "users/avatars/" + user.getId();

            ApiResponse<String> storageResponse = storageClient.confirmFile(
                    new FileMoveRequest(
                            request.avatarKey(),
                            destinationPath
                    )
            );

            if (storageResponse.getErrorCode() != null)
                throw new RuntimeException("Lỗi di chuyển file: " + storageResponse.getErrorMessage());
            user.setAvatarUrl(storageResponse.getData());

            kafkaTemplate.send(storageDeleteFileTopic, oldAvatarUrl);
        }
    }

    @Override
    public void updateUserPassword(UpdatePasswordRequest request) {
        String keycloakId = UserContext.get().getKeycloakId();
        String email = UserContext.get().getEmail();

        verifyOldPassword(email, request.oldPassword());

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.newPassword());
        credential.setTemporary(false);

        try {
            keycloak.realm(currentRealm)
                    .users()
                    .get(keycloakId)
                    .resetPassword(credential);

            log.info("Successfully updated password for user: {}", keycloakId);
        } catch (BadRequestException e) {
            log.error("Password policy violation for user {}: {}", keycloakId, e.getMessage());
            throw new RuntimeException("Mật khẩu của bạn không đạt chuẩn!");
        } catch (Exception e) {
            log.error("Unexpected error during password update for user {}: {}", keycloakId, e.getMessage());
            throw new RuntimeException("Có lỗi đã xảy ra khi cố cập nhật mật khẩu.");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public UserPermissionsResponse getUserPermissions(String keycloakId) {
        User user = userRepository
                .findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng"));

        Set<String> permissions = (user.getRole() != null)
                ? user.getRole().getPermissions().stream().map(Permission::getName).collect(Collectors.toSet())
                : Collections.emptySet();

        String role = user.getRole() != null ? user.getRole().getName() : "";

        return new UserPermissionsResponse(user.getEmail(), role, permissions);
    }

    @Override
    public void assignRoleByName(String keycloakId, String roleName) {
        User user = userRepository
                .findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng"));

        Role role = roleRepository
                .findByName(roleName.toUpperCase())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy role: " + roleName));

        user.setRole(role);
        userRepository.save(user);
    }

    private void verifyOldPassword(String email, String oldPassword) {
        try (Keycloak tempKeycloak = KeycloakBuilder
                .builder()
                .serverUrl(serverUrl)
                .realm(currentRealm)
                .clientId(clientId)
                .grantType(OAuth2Constants.PASSWORD)
                .username(email)
                .password(oldPassword)
                .build()) {

            tempKeycloak.tokenManager().getAccessToken();
        } catch (Exception e) {
            log.warn("Failed password verification attempt for user: {}", email);
            throw new RuntimeException("Mật khẩu cũ không chính xác!");
        }
    }

}
