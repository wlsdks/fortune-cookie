package io.github.wlsdks.fortunecookie.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 포춘 쿠키 보안 관련 설정 속성을 정의하는 클래스입니다.
 * 사용자가 application.yml 또는 properties 파일에서 보안 관련 설정을 정의하면 이 클래스의 속성으로 로드됩니다.
 * fortune-cookie.security.enabled=false 같은 설정이 이 클래스로 매핑됩니다.
 */
@Setter
@Getter
@Component
@ConfigurationProperties("fortune-cookie.security")
public class FortuneSecurityProperties {

    /**
     * Spring Security 통합 활성화 여부
     */
    private boolean enabled = true;

    /**
     * 보안 관련 플레이스홀더 활성화 여부
     */
    private boolean placeholdersEnabled = true;

    /**
     * 헤더 값 검증 활성화 여부
     */
    private boolean validateHeaderValues = true;

    /**
     * 권한이 필요한 포춘 메시지에 대한 기본 역할
     */
    private String defaultRole = "ROLE_USER";

}