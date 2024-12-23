package io.github.wlsdks.fortunecookie.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wlsdks.fortunecookie.interceptor.FortuneCookieInterceptor;
import io.github.wlsdks.fortunecookie.interceptor.FortuneCookieResponseAdvice;
import io.github.wlsdks.fortunecookie.properties.FortuneCookieProperties;
import io.github.wlsdks.fortunecookie.provider.DefaultFortuneProvider;
import io.github.wlsdks.fortunecookie.provider.FortuneProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 포춘 쿠키 라이브러리의 자동 설정을 담당하는 클래스입니다.
 * Spring Boot의 자동 설정 메커니즘을 통해 필요한 빈들을 자동으로 등록합니다.
 */
@EnableConfigurationProperties(FortuneCookieProperties.class)
@AutoConfiguration  // @Configuration 대신 @AutoConfiguration 사용
@ConditionalOnProperty(
        prefix = "fortune-cookie",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class FortuneCookieAutoConfiguration implements WebMvcConfigurer {

    private final FortuneCookieProperties properties;
    private final ObjectMapper objectMapper;

    public FortuneCookieAutoConfiguration(FortuneCookieProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }


    /**
     * 포춘 메시지 소스 빈을 구성합니다.
     * 포춘 메시지를 properties에 지정된 경로에서 가져옵니다.
     */
    @Bean
    @ConditionalOnMissingBean
    public MessageSource messageSource() {  // fortuneMessageSource에서 messageSource로 변경
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("fortunes/fortunes");  // 경로 직접 지정
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }

    /**
     * 포춘 메시지 제공자 빈을 구성합니다.
     * 사용자가 직접 FortuneProvider를 구현하여 등록하지 않은 경우
     * 기본 구현체인 DefaultFortuneProvider를 사용합니다.
     */
    @Bean
    @ConditionalOnMissingBean(FortuneProvider.class)
    public FortuneProvider fortuneProvider(MessageSource messageSource) {
        return new DefaultFortuneProvider(messageSource, properties);
    }

    /**
     * 포춘 쿠키 응답 어드바이스 빈을 구성합니다.
     * HTTP 응답에 포춘 메시지를 자동으로 추가하는 역할을 합니다.
     */
    @Bean
    @ConditionalOnMissingBean
    public FortuneCookieResponseAdvice fortuneCookieResponseAdvice(FortuneProvider fortuneProvider,
                                                                   FortuneCookieProperties properties) {
        // 실제 어드바이스 빈 생성
        return new FortuneCookieResponseAdvice(fortuneProvider, properties);
    }

    /**
     * 포춘 쿠키 인터셉터 빈을 구성합니다.
     */
    @Bean
    @ConditionalOnMissingBean
    public FortuneCookieInterceptor fortuneCookieInterceptor(FortuneProvider fortuneProvider,
                                                             FortuneCookieProperties props,
                                                             MessageSource messageSource) {
        return new FortuneCookieInterceptor(fortuneProvider, props, messageSource);
    }

    /**
     * 인터셉터를 글로벌하게 추가합니다.
     * 모든 요청에 대해 인터셉터가 실행되지만, 내부 로직에서 어노테이션을 검사하여 적용 여부를 결정합니다.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(fortuneCookieInterceptor(fortuneProvider(messageSource()), properties, messageSource()))
                .addPathPatterns("/**");
    }

}
