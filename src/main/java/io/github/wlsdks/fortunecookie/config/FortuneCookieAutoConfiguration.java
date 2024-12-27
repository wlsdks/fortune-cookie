package io.github.wlsdks.fortunecookie.config;

import io.github.wlsdks.fortunecookie.interceptor.FortuneCookieInterceptor;
import io.github.wlsdks.fortunecookie.interceptor.FortuneCookieResponseAdvice;
import io.github.wlsdks.fortunecookie.interceptor.module.GameModule;
import io.github.wlsdks.fortunecookie.interceptor.module.impl.NumberGuessGame;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 포춘 쿠키 라이브러리의 자동 설정을 담당하는 클래스입니다.
 * Spring Boot의 자동 설정 메커니즘을 통해 필요한 빈들을 자동으로 등록합니다.
 * fortune-cookie.enabled가 true일 때만 전체 로직 활성화됩니다.
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

    public FortuneCookieAutoConfiguration(FortuneCookieProperties properties) {
        this.properties = properties;
    }

    /**
     * 포춘 메시지 소스 빈을 구성합니다.
     * 포춘 메시지를 properties에 지정된 경로에서 가져옵니다.
     * ResourceBundleMessageSource를 생성해 fortunes/fortunes 경로 지정(영문, 한글 등).
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
     * 사용자가 직접 FortuneProvider를 구현하여 등록하지 않은 경우 기본 구현체인 DefaultFortuneProvider를 사용합니다.
     */
    @Bean
    @ConditionalOnMissingBean(FortuneProvider.class)
    public FortuneProvider fortuneProvider(MessageSource messageSource) {
        return new DefaultFortuneProvider(messageSource, properties);
    }

    /**
     * 포춘 쿠키 응답 어드바이스 빈을 구성합니다.
     * HTTP 응답에 포춘 메시지를 자동으로 추가하는 역할을 합니다. (JSON 바디에 메시지 삽입 담당)
     */
    @Bean
    @ConditionalOnMissingBean
    public FortuneCookieResponseAdvice fortuneCookieResponseAdvice(FortuneCookieProperties properties) {
        // 실제 어드바이스 빈 생성
        return new FortuneCookieResponseAdvice(properties);
    }

    /**
     * 포춘 쿠키 인터셉터 빈을 구성합니다.
     * 컨트롤러 진입 전후로 헤더, 바디 메시지를 처리 및 추가합니다.
     */
    @Bean
    @ConditionalOnMissingBean
    public FortuneCookieInterceptor fortuneCookieInterceptor(FortuneProvider fortuneProvider,
                                                             FortuneCookieProperties props) {
        // 게임 모듈 리스트
        List<GameModule> gameModuleList = new ArrayList<>(List.of());
        // 숫자 맞추기 게임 추가
        gameModuleList.add(new NumberGuessGame(properties, messageSource(), new Random()));
        return new FortuneCookieInterceptor(fortuneProvider, props, gameModuleList);
    }

    /**
     * 인터셉터를 글로벌하게 추가합니다.
     * 모든 URL을 대상으로 Interceptor가 실행되며, 내부 로직에서 실제로 @FortuneCookie가 붙은 곳만 포춘 메시지를 삽입합니다.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(fortuneCookieInterceptor(fortuneProvider(messageSource()), properties))
                .addPathPatterns("/**");
    }

}
