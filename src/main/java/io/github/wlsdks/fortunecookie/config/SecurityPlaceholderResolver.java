package io.github.wlsdks.fortunecookie.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.stream.Collectors;

import static io.github.wlsdks.fortunecookie.common.Constant.*;

@Slf4j
public class SecurityPlaceholderResolver {

    /**
     * 보안 컨텍스트에서 플레이스홀더 값을 해석합니다.
     * Spring Security의 인증 정보에서 값을 가져와 포춘 메시지의 플레이스홀더를 실제 값으로 변환합니다.
     * 예를들어 {username}이라는 포춘 메시지의 플레이스홀더를 현재 로그인한 사용자의 이름으로 바꿔줍니다.
     *
     * @param placeholderKey 플레이스홀더 키 (username, roles 등)
     * @return 해석된 값 또는 null (인증 정보가 없거나 키가 없는 경우)
     */
    public String resolveSecurityPlaceholder(String placeholderKey) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (isAuthenticatedUser(auth)) {
                switch (placeholderKey) {
                    // 사용자 이름
                    case USERNAME -> {
                        return auth.getName();
                    }
                    // 사용자 역할
                    case ROLES -> {
                        return auth.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.joining(","));
                    }
                    case PRINCIPAL -> {
                        Object principal = auth.getPrincipal();
                        return principal != null ? principal.toString() : null;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Failed to access security context: {}", e.getMessage());
            return null;
        }

        return null;
    }

    /**
     * 사용자가 인증되었는지 확인합니다.
     */
    private boolean isAuthenticatedUser(Authentication auth) {
        return auth != null && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);
    }

}