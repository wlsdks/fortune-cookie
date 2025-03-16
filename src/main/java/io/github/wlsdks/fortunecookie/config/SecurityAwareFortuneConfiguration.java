package io.github.wlsdks.fortunecookie.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(name = "org.springframework.security.core.Authentication")
public class SecurityAwareFortuneConfiguration {

    @Bean
    public SecurityPlaceholderResolver securityPlaceholderResolver() {
        return new SecurityPlaceholderResolver();
    }

}
