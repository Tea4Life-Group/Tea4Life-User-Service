package tea4life.user_service.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tea4life.user_service.context.UserContext;
import tea4life.user_service.dto.response.UserStatusResponse;
import tea4life.user_service.dto.response.constant.UserStatus;
import tea4life.user_service.model.User;
import tea4life.user_service.repository.UserRepository;

/**
 * Admin 2/6/2026
 *
 **/
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserStatusServiceImpl implements tea4life.user_service.service.UserStatusService {

    UserRepository userRepository;

    StringRedisTemplate stringRedisTemplate;

    static final String PENDING_USER_PREFIX = "PENDING_USER:";

    @Override
    public UserStatusResponse checkUserStatus() {
        String email = UserContext.get().getEmail();

        return userRepository
                .findByEmail(email)
                .map(this::buildSuccessResponse)
                .orElseGet(() -> checkPendingStatus(email));
    }

    private UserStatusResponse buildSuccessResponse(User user) {
        return new UserStatusResponse(
                UserStatus.SUCCESS,
                true,
                user.getOnBoarded(),
                user.getFullName(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().getName() : "",
                user.getAvatarUrl());
    }

    private UserStatusResponse checkPendingStatus(String email) {
        boolean isPending = Boolean.TRUE.equals(stringRedisTemplate.hasKey(PENDING_USER_PREFIX + email));

        return isPending
                ? new UserStatusResponse(UserStatus.PROCESSING, false, false, null, null, null, null)
                : new UserStatusResponse(UserStatus.NOT_FOUND, false, false, null, null, null, null);
    }

}
