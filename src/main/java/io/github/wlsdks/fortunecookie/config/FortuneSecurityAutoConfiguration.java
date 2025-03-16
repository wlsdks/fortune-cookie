package io.github.wlsdks.fortunecookie.config;

import io.github.wlsdks.fortunecookie.properties.FortuneSecurityProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 포춘 쿠키 보안 관련 자동 설정 클래스입니다. 필요한 빈들을 자동으로 등록합니다.
 * Spring Security가 클래스패스에 존재하고, fortune-cookie.security.enabled=true인 경우에만 활성화됩니다.
 */
@Configuration
@ConditionalOnClass(name = "org.springframework.security.core.Authentication")
@ConditionalOnProperty(
        prefix = "fortune-cookie.security",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@EnableConfigurationProperties(FortuneSecurityProperties.class)
public class FortuneSecurityAutoConfiguration {

    @Bean
    public SecurityPlaceholderResolver securityPlaceholderResolver() {
        return new SecurityPlaceholderResolver();
    }

}