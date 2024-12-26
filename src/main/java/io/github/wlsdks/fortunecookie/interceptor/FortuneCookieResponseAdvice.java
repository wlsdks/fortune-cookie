package io.github.wlsdks.fortunecookie.interceptor;

import io.github.wlsdks.fortunecookie.annotation.FortuneCookie;
import io.github.wlsdks.fortunecookie.dto.FortuneWrapper;
import io.github.wlsdks.fortunecookie.properties.FortuneCookieProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 포춘 쿠키 메시지를 HTTP 응답에 자동으로 추가하는 ResponseBodyAdvice 구현체입니다.
 */
@Slf4j
@ControllerAdvice
public class FortuneCookieResponseAdvice implements ResponseBodyAdvice<Object> {

    public static final String FORTUNE_BODY = "fortuneBody";
    private final FortuneCookieProperties properties;

    /**
     * FortuneCookieResponseAdvice를 생성합니다.
     *
     * @param properties 포춘 쿠키 설정 정보
     */
    public FortuneCookieResponseAdvice(FortuneCookieProperties properties) {
        this.properties = properties;
    }

    /**
     * 이 Advice가 응답을 처리할지 여부를 결정합니다.
     * 포춘 쿠키 기능이 활성화되어 있고, JSON 응답인 경우에만 처리합니다.
     * 또한 @FortuneCookie 어노테이션이 적용된 경우에만 처리합니다.
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        if (!properties.isEnabled() ||
                !MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType)) {
            return false;
        }

        // 현재 요청 핸들러 가져오기
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes)) {
            return false;
        }

        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);

        if (!(handler instanceof HandlerMethod)) {
            return false;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        FortuneCookie annotation = AnnotationUtils.findAnnotation(
                handlerMethod.getMethod(), FortuneCookie.class);

        if (annotation == null) {
            annotation = AnnotationUtils.findAnnotation(
                    handlerMethod.getBeanType(), FortuneCookie.class);
        }

        return annotation != null;
    }


    /**
     * 응답 본문이 클라이언트로 전송되기 전에 포춘 메시지를 추가합니다.
     * 설정에 따라 HTTP 헤더나 JSON 응답 본문에 메시지를 추가할 수 있습니다.
     *
     * @param body                  원본 응답 본문
     * @param returnType            컨트롤러 메서드의 반환 타입
     * @param selectedContentType   선택된 컨텐츠 타입
     * @param selectedConverterType 선택된 메시지 컨버터 타입
     * @param request               현재 HTTP 요청
     * @param response              현재 HTTP 응답
     * @return 수정된 응답 본문
     */
    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        // 1. 포춘 쿠키 기능이 꺼져 있으면 처리하지 않음
        if (!properties.isEnabled()) {
            return body;
        }

        // 2. JSON 컨버터가 아닌 경우 처리하지 않음
        if (!MappingJackson2HttpMessageConverter.class.isAssignableFrom(selectedConverterType)) {
            return body;
        }

        // 3. Interceptor에서 저장한 바디용 포춘 메시지 읽기
        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        String bodyFortune = (String) servletRequest.getAttribute(FORTUNE_BODY);

        // 4. 바디에 메시지 추가 기능이 꺼져 있거나, fortuneBody가 null인 경우 처리하지 않음
        if (!properties.isIncludeInResponse() || bodyFortune == null) {
            return body;
        }

        // 5) 만약 body가 Map이면, 기존 로직대로 "fortune" 필드 추가
        if (body instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = new HashMap<>((Map<String, Object>) body);
            map.put(properties.getResponseFortuneName(), bodyFortune);
            return map;
        }

        // 6) 그 외 타입이면, 우리가 만든 FortuneWrapper<T>로 감싸서 반환
        //    예: FortuneWrapper<YourDto>
        return new FortuneWrapper<>(body, bodyFortune);
    }

}