package io.github.wlsdks.fortunecookie.config;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.stream.Collectors;

import static io.github.wlsdks.fortunecookie.common.Constant.ROLES;
import static io.github.wlsdks.fortunecookie.common.Constant.USERNAME;

public class SecurityPlaceholderResolver {

    public String resolveSecurityPlaceholder(String placeholderKey) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            switch (placeholderKey) {
                case USERNAME -> {
                    return auth.getName();
                }
                case ROLES -> {
                    return auth.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.joining(","));
                }
                // 추가 필요한 보안 관련 플레이스홀더
            }
        }
        return null;
    }

}