package tea4life.user_service.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import tea4life.user_service.context.UserContext;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Admin 2/7/2026
 *
 **/
@Component
public class UserContextFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest servletRequest,
            ServletResponse servletResponse,
            FilterChain filterChain
    ) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;

        String email = httpRequest.getHeader("X-User-Email");
        String userKeycloakId = httpRequest.getHeader("X-User-KeycloakId");
        String role = httpRequest.getHeader("X-User-Role");
        String authoritiesRaw = httpRequest.getHeader("X-User-Authorities");

        /**
         * =============================================
         * ĐỒNG BỘ HÓA SPRING SECURITY CONTEXT
         * Chuyển đổi chuỗi Permissions từ Header thành danh sách SimpleGrantedAuthority
         * để kích hoạt các cơ chế bảo mật như @PreAuthorize trên Controller.
         * =============================================
         **/

        List<SimpleGrantedAuthority> authorities = Collections.emptyList();

        if (authoritiesRaw != null && !authoritiesRaw.isBlank()) {
            authorities = Arrays.stream(authoritiesRaw.split(","))
                    .filter(auth -> !auth.isBlank())
                    .map(SimpleGrantedAuthority::new)
                    .toList();
        }

        if (hasText(role)) {
            List<SimpleGrantedAuthority> roleAuthorities = List.of(
                    new SimpleGrantedAuthority(normalizeRole(role))
            );
            authorities = authorities.isEmpty()
                    ? roleAuthorities
                    : java.util.stream.Stream
                    .concat(authorities.stream(), roleAuthorities.stream())
                    .distinct()
                    .toList();
        }

        if (hasText(email) || hasText(userKeycloakId) || !authorities.isEmpty()) {
            var auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
        } else SecurityContextHolder.clearContext();


        /**
         * =============================================
         * THIẾT LẬP CONTEXT ĐỊNH DANH NGƯỜI DÙNG
         * Lưu trữ thông tin định danh vào ThreadLocal giúp tầng Service dễ dàng truy cập
         * mà không cần thông qua lớp bảo mật phức tạp của Spring.
         * =============================================
         **/

        if (hasText(email) || hasText(userKeycloakId)) {
            UserContext context = UserContext
                    .builder()
                    .email(email)
                    .keycloakId(userKeycloakId)
                    .build();

            UserContext.set(context);
        } else UserContext.clear();


        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContext.clear();
            SecurityContextHolder.clearContext();
        }

    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalizeRole(String role) {
        return role.replaceFirst("^ROLE_", "").toUpperCase();
    }
}
