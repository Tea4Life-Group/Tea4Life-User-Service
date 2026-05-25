package tea4life.user_service.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tea4life.user_service.model.Role;
import tea4life.user_service.model.User;
import tea4life.user_service.model.constant.RoleName;
import tea4life.user_service.repository.RoleRepository;
import tea4life.user_service.repository.UserRepository;

import java.util.concurrent.TimeUnit;

/**
 * Admin 2/3/2026
 *
 **/
@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserSyncConsumer {

    UserRepository userRepository;
    RoleRepository roleRepository;
    StringRedisTemplate stringRedisTemplate;
    ObjectMapper objectMapper;

    @KafkaListener(topics = "${spring.kafka.topic.user-registration}")
    public void listenUserRegistration(String message) {
        try {
            JsonNode payload = objectMapper.readTree(message);
            String action = payload.path("action").asText("CREATE");
            String source = payload.path("source").asText("UNKNOWN");
            String userId = payload.path("userId").asText();

            log.info(
                    ">>>> [KAFKA-IN] [{}] Source: {} | UserID: {} | Email: {}",
                    String.format("%-6s", action), String.format("%-5s", source),
                    userId, payload.path("email").asText()
            );

            switch (action) {
                case "CREATE" -> handleCreateUser(payload, userId);

                case "DELETE" -> {

                }

                case "UPDATE" -> {

                }

                default -> {
                    log.warn("Unrecognized action: {}", action);
                }
            }

        } catch (Exception e) {
            log.error("Failed to process Kafka message: {}", e.getMessage());
        }
    }

    private void handleCreateUser(JsonNode payload, String keycloakId) {
        String email = payload.path("email").asText();
        stringRedisTemplate.opsForValue().set(
                "PENDING_USER:" + email,
                "processing",
                30,
                TimeUnit.SECONDS);

        try {
            if (userRepository.existsByKeycloakId(keycloakId)) {
                log.warn("User {} already exists. Skipping.", keycloakId);
                return;
            }

            User newUser = new User();
            newUser.setKeycloakId(keycloakId);
            newUser.setEmail(email);
            newUser.setOnBoarded(false);
            newUser.setRole(resolveDefaultRole());
            userRepository.save(newUser);

            log.info("Successfully persisted new user to DB.");
        } catch (Exception e) {
            log.error("Error saving user: {}", e.getMessage());
            throw e;
        } finally {
            stringRedisTemplate.delete("PENDING_USER:" + email);
        }
    }

    private Role resolveDefaultRole() {
        return roleRepository
                .findByName(RoleName.MEMBER)
                .orElseThrow(() -> new IllegalStateException("Missing default role: " + RoleName.MEMBER));
    }

}
