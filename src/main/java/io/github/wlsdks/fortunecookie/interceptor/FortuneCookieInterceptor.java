package io.github.wlsdks.fortunecookie.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wlsdks.fortunecookie.properties.FortuneCookieProperties;
import io.github.wlsdks.fortunecookie.provider.FortuneProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

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
        if (!properties.isEnabled()) {
            return;
        }

        String fortune = fortuneProvider.getFortune(request.getLocale());

        // 헤더에 포춘 메시지 추가 (응답이 커밋되기 전)
        if (properties.isIncludeHeader()) {
            response.setHeader(properties.getHeaderName(), fortune);
        }
    }

}