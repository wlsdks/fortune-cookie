package io.github.wlsdks.fortunecookie.interceptor;

import io.github.wlsdks.fortunecookie.properties.FortuneCookieProperties;
import io.github.wlsdks.fortunecookie.provider.FortuneProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Locale;

/**
 * 포춘 쿠키 메시지를 HTTP 응답 헤더에 추가하는 인터셉터입니다.
 */
@Slf4j
@Component
public class FortuneCookieInterceptor implements HandlerInterceptor {

    private final FortuneProvider fortuneProvider;
    private final FortuneCookieProperties properties;

    public FortuneCookieInterceptor(FortuneProvider fortuneProvider,
                                    FortuneCookieProperties properties) {
        this.fortuneProvider = fortuneProvider;
        this.properties = properties;
    }

    /**
     * preHandle : 컨트롤러 실행 전에 호출
     * - 헤더는 영어로 고정
     * - 바디는 요청 로케일에 맞춤
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.debug(">>> FortuneCookieInterceptor preHandle called");
        if (!properties.isEnabled()) {
            return true; // 기능 비활성화이면 그냥 통과
        }

        // 포춘 키 생성
        String fortuneKey = fortuneProvider.generateFortuneKey();
        log.debug(">>> preHandle: generated fortuneKey: {}", fortuneKey);

        // 헤더용 메시지 (영어로 고정)
        Locale headerLocale = Locale.ENGLISH;
        String headerFortune = fortuneProvider.getFortune(fortuneKey, headerLocale);
        log.debug(">>> preHandle: headerFortune: {}", headerFortune);

        if (properties.isIncludeHeader()) {
            response.setHeader(properties.getHeaderName(), headerFortune);
            log.debug(">>> preHandle: headerFortune set: {}", headerFortune);
            String retrieved = response.getHeader(properties.getHeaderName());
            log.debug(">>> preHandle: fortune header retrieved from response: {}", retrieved);
        }

        // 바디용 메시지 (요청 로케일)
        Locale bodyLocale = request.getLocale();
        String bodyFortune = fortuneProvider.getFortune(fortuneKey, bodyLocale);
        log.debug(">>> preHandle: bodyFortune: {}", bodyFortune);

        // Request Attribute에 저장 (ResponseBodyAdvice에서 사용)
        request.setAttribute("fortuneBody", bodyFortune);

        return true; // 컨트롤러 계속 진행
    }

    /**
     * postHandle : 컨트롤러 실행 후, View 렌더링 직전에 호출
     * - 현재 필요 없음
     */
    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) throws Exception {
        log.debug(">>> FortuneCookieInterceptor postHandle called");
        // 헤더 설정은 preHandle에서 이미 했으므로 별도 작업 없음
    }

    /**
     * afterCompletion : 모든 요청 처리가 끝난 후 호출
     * - 응답이 이미 커밋된 상태일 수 있어서 헤더 추가가 무효화될 확률이 큼
     */
    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) throws Exception {
        log.debug(">>> FortuneCookieInterceptor afterCompletion called");
        // 헤더 설정은 preHandle에서 이미 했으므로 별도 작업 없음
    }

}