package io.github.wlsdks.fortunecookie.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wlsdks.fortunecookie.properties.FortuneCookieProperties;
import io.github.wlsdks.fortunecookie.provider.FortuneProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 포춘 쿠키 메시지를 HTTP 응답에 자동으로 추가하는 ResponseBodyAdvice 구현체입니다.
 * Spring MVC의 응답 처리 과정에 개입하여 JSON 응답이나 헤더에 포춘 메시지를 추가합니다.
 * <p>
 * 주요 기능:
 * - JSON 응답 본문에 포춘 메시지 추가
 * - HTTP 헤더에 포춘 메시지 추가
 * - 다국어(i18n) 지원
 * - 설정을 통한 기능 활성화/비활성화
 * <p>
 * 설정 예시 (application.yml):
 * fortune-cookie:
 * enabled: true
 * include-header: true
 * header-name: X-Fortune-Cookie
 * include-in-response: true
 * response-fortune-name: fortune
 */
@Slf4j
@ControllerAdvice
public class FortuneCookieResponseAdvice implements ResponseBodyAdvice<Object> {

    private final FortuneProvider fortuneProvider;
    private final FortuneCookieProperties properties;
    private final ObjectMapper objectMapper;

    /**
     * FortuneCookieResponseAdvice를 생성합니다.
     *
     * @param fortuneProvider 포춘 메시지를 제공하는 프로바이더
     * @param properties      포춘 쿠키 설정 정보
     * @param objectMapper    JSON 처리를 위한 ObjectMapper
     */
    public FortuneCookieResponseAdvice(FortuneProvider fortuneProvider,
                                       FortuneCookieProperties properties,
                                       ObjectMapper objectMapper) {
        this.fortuneProvider = fortuneProvider;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * 이 Advice가 응답을 처리할지 여부를 결정합니다.
     * 포춘 쿠키 기능이 활성화되어 있고, JSON 응답인 경우에만 처리합니다.
     *
     * @param returnType    컨트롤러 메서드의 반환 타입
     * @param converterType 메시지 컨버터 타입
     * @return 응답을 처리해야 하면 true, 아니면 false
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return properties.isEnabled() && converterType.isAssignableFrom(MappingJackson2HttpMessageConverter.class);
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
        // 포춘 쿠키 기능이 비활성화되어 있으면 처리하지 않음
        if (!(body instanceof Map)) {
            return body;
        }

        // 포춘 메시지를 가져와서 응답 본문에 추가
        String fortune = fortuneProvider.getFortune(request.getHeaders().getAcceptLanguage().isEmpty() ?
                Locale.getDefault() :
                Locale.forLanguageTag(request.getHeaders().getAcceptLanguage().get(0).toString()));

        // 응답 본문에 포춘 메시지를 추가
        Map<String, Object> map = new HashMap<>((Map<String, Object>) body);
        map.put(properties.getResponseFortuneName(), fortune);
        return map;
    }

}