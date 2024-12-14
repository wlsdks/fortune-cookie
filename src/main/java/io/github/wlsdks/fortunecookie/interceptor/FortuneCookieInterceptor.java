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
     * 1) preHandle : 컨트롤러 실행 전에 호출
     * - 예시: 헤더와 바디 메시지를 생성하되, 헤더는 영어로 강제, 바디는 로케일에 맞춤
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.debug(">>> FortuneCookieInterceptor preHandle called");
        if (!properties.isEnabled()) {
            return true; // 기능 비활성화이면 그냥 통과
        }

        // 바디용 메시지(요청 로케일)
        Locale bodyLocale = request.getLocale();
        String bodyFortune = fortuneProvider.getFortune(bodyLocale);
        log.debug(">>> preHandle: bodyFortune: {}", bodyFortune);

        // 헤더용 메시지(영어로 고정)
        Locale headerLocale = Locale.ENGLISH;
        String headerFortune = fortuneProvider.getFortune(headerLocale);

        if (properties.isIncludeHeader()) {
            response.setHeader(properties.getHeaderName(), headerFortune);
            log.debug(">>> preHandle: headerFortune set: {}", headerFortune);
            String retrieved = response.getHeader(properties.getHeaderName());
            log.debug(">>> preHandle: fortune header retrieved from response: {}", retrieved);
        }

        // preHandle은 boolean 리턴; true면 컨트롤러 계속 진행
        return true;
    }

    /**
     * 2) postHandle : 컨트롤러 실행 후, View 렌더링 직전 호출
     * - JSON 응답이라면 이미 Body 변환이 이루어지는 시점과 맞물릴 수 있음
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) throws Exception {
        log.debug(">>> FortuneCookieInterceptor postHandle called");
        if (!properties.isEnabled()) {
            return;
        }

        // 바디용 메시지(요청 로케일)
        Locale bodyLocale = request.getLocale();
        String bodyFortune = fortuneProvider.getFortune(bodyLocale);
        log.debug(">>> postHandle: bodyFortune: {}", bodyFortune);

        // 헤더용 메시지(영어로 고정)
        Locale headerLocale = Locale.ENGLISH;
        String headerFortune = fortuneProvider.getFortune(headerLocale);

        if (properties.isIncludeHeader()) {
            response.setHeader(properties.getHeaderName(), headerFortune);
            log.debug(">>> postHandle: headerFortune set: {}", headerFortune);
            String retrieved = response.getHeader(properties.getHeaderName());
            log.debug(">>> postHandle: fortune header retrieved from response: {}", retrieved);
        }
    }

    /**
     * 3) afterCompletion : 모든 요청 처리가 끝난 후(뷰 렌더링, Body 변환 포함) 호출
     * - 응답이 이미 커밋된 상태일 수 있어서 헤더 추가가 무효화될 확률이 큼
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        log.debug(">>> FortuneCookieInterceptor afterCompletion called");
        if (!properties.isEnabled()) {
            return;
        }

        // 바디 로케일
        Locale bodyLocale = request.getLocale();
        String bodyFortune = fortuneProvider.getFortune(bodyLocale);
        log.debug(">>> afterCompletion: bodyFortune: {}", bodyFortune);

        // 헤더는 영어 강제
        Locale headerLocale = Locale.ENGLISH;
        String headerFortune = fortuneProvider.getFortune(headerLocale);

        if (properties.isIncludeHeader()) {
            response.setHeader(properties.getHeaderName(), headerFortune);
            log.debug(">>> afterCompletion: headerFortune set: {}", headerFortune);
            String retrieved = response.getHeader(properties.getHeaderName());
            log.debug(">>> afterCompletion: fortune header retrieved from response: {}", retrieved);
        }
    }

}