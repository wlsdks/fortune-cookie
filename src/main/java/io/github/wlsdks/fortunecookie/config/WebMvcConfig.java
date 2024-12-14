package io.github.wlsdks.fortunecookie.config;

import io.github.wlsdks.fortunecookie.interceptor.FortuneCookieInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final FortuneCookieInterceptor fortuneCookieInterceptor;

    public WebMvcConfig(FortuneCookieInterceptor fortuneCookieInterceptor) {
        this.fortuneCookieInterceptor = fortuneCookieInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(fortuneCookieInterceptor);
    }

}