package io.github.wlsdks.fortunecookie.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wlsdks.fortunecookie.properties.FortuneCookieProperties;
import io.github.wlsdks.fortunecookie.provider.FortuneProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Component
public class FortuneCookieInterceptor implements HandlerInterceptor {

    private final FortuneProvider fortuneProvider;
    private final FortuneCookieProperties properties;

    public FortuneCookieInterceptor(FortuneProvider fortuneProvider,
                                    FortuneCookieProperties properties,
                                    ObjectMapper objectMapper) {
        this.fortuneProvider = fortuneProvider;
        this.properties = properties;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) throws Exception {
        log.debug(">>> FortuneCookieInterceptor postHandle called");

        // fortune-cookie.enabled가 true일 때만 수행
        if (properties.isEnabled()) {
            // Locale 정보를 이용해 메시지를 가져옴
            String fortune = fortuneProvider.getFortune(request.getLocale());

            // fortune-cookie.include-header가 true일 때만 헤더로 설정
            if (properties.isIncludeHeader()) {
                response.setHeader(properties.getHeaderName(), fortune);

                // 1) 바로 fortune 문자열 로그로 출력
                log.debug(">>> fortune header set: {}", fortune);

                // 2) response 객체에서 다시 헤더를 꺼내 로그로 출력
                String headerVal = response.getHeader(properties.getHeaderName());
                log.debug(">>> fortune header retrieved from response: {}", headerVal);
            }
        }
    }

}