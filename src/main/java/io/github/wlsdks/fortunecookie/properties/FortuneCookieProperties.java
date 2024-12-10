package io.github.wlsdks.fortunecookie.properties;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Set;

@Getter
@ConfigurationProperties("fortune-cookie")
public class FortuneCookieProperties {

    // 포춘 쿠키 기능 활성화 여부
    private boolean enabled = true;

    // 응답 헤더에 포춘 메시지를 포함할지에 대한 여부
    private boolean includeHeader = true;

    // 포춘 메시지를 포함할 헤더 이름
    private String headerName = "X-Fortune-Cookie";

    // JSON 응답에 포춘 메시지를 포함할지에 대한 여부
    private boolean includeInResponse = true;

    // JSON 응답에 포함될 때 사용할 필드 이름
    private String responseFortuneName = "fortune";

    // 특정 HTTP 상태 코드에만 포춘 메시지를 포함할지 설정 (비어있으면 모든 상태 코드에 포함)
    private Set<Integer> includedStatusCodes = new HashSet<>();

    // 포춘 메시지를 제외할 URL 패턴들
    private Set<String> excludePatterns = new HashSet<>();

    // 에러 응답에도 포춘 메시지를 포함할지 여부
    private boolean includeOnError = true;

    // 포춘 메시지의 최대 길이 (0 = 무제한)
    private int maxFortuneLength = 0;

    // 디버그 모드 활성화 여부 (true일 경우 로그에 선택된 포춘 메시지 정보가 출력됩니다.)
    private boolean debug = false;

    // 커스텀 포춘 메시지 경로 (기본 메시지 대신 사용자 정의 메시지를 사용할 경우 설정)
    private String customMessagesPath = "";

}
