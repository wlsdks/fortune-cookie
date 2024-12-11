package io.github.wlsdks.fortunecookie.interceptor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.wlsdks.fortunecookie.properties.FortuneCookieProperties;
import io.github.wlsdks.fortunecookie.provider.FortuneProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.nio.charset.StandardCharsets;

/**
 * HTTP 응답에 포춘 쿠키 메시지를 추가하는 인터셉터입니다.
 * 설정에 따라 HTTP 헤더나 JSON 응답 본문에 메시지를 추가할 수 있습니다.
 */
@Slf4j
public class FortuneCookieInterceptor implements HandlerInterceptor {

    private final FortuneProvider fortuneProvider;
    private final FortuneCookieProperties fortuneCookieProperties;
    private final ObjectMapper objectMapper;

    /**
     * FortuneCookieInterceptor를 생성합니다.
     *
     * @param fortuneProvider         포춘 메시지를 제공하는 프로바이더
     * @param fortuneCookieProperties 포춘 쿠키 설정 정보
     * @param objectMapper            JSON 처리를 위한 ObjectMapper
     */
    public FortuneCookieInterceptor(FortuneProvider fortuneProvider,
                                    FortuneCookieProperties fortuneCookieProperties,
                                    ObjectMapper objectMapper) {
        this.fortuneProvider = fortuneProvider;
        this.fortuneCookieProperties = fortuneCookieProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * HTTP 요청 처리가 완료된 후 포춘 메시지를 응답에 추가합니다.
     * 설정에 따라 헤더나 JSON 응답에 메시지가 포함됩니다.
     *
     * @param request  현재 HTTP 요청
     * @param response 현재 HTTP 응답
     * @param handler  요청을 처리한 핸들러
     * @param ex       처리 중 발생한 예외 (있는 경우)
     * @throws Exception JSON 처리 중 발생할 수 있는 예외
     */
    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) throws Exception {

        // 포춘 메시지 포함 여부를 체크
        if (!shouldIncludeFortune(request, response)) {
            return;
        }

        // 현재 로케일에 맞는 포춘 메시지 가져오기
        String fortune = fortuneProvider.getFortune(request.getLocale());

        // 디버그 모드에서는 선택된 메시지를 로그로 출력
        if (fortuneCookieProperties.isDebug()) {
            log.debug("Selected fortune: {}", fortune);
        }

        // 설정된 최대 길이를 초과하는 경우 메시지 자르기
        if (fortuneCookieProperties.getMaxFortuneLength() > 0 &&
                fortune.length() > fortuneCookieProperties.getMaxFortuneLength()
        ) {
            fortune = fortune.substring(0, fortuneCookieProperties.getMaxFortuneLength()) + "...";
        }

        // 설정에 따라 헤더에 포춘 메시지 추가
        if (fortuneCookieProperties.isIncludeHeader()) {
            response.setHeader(fortuneCookieProperties.getHeaderName(), fortune);
        }

        // JSON 응답인 경우에만 응답 본문에 포춘 메시지 추가
        if (fortuneCookieProperties.isIncludeInResponse() &&
                isJsonResponse(response)) {
            modifyJsonResponse(response, fortune);
        }
    }

    /**
     * 현재 요청/응답에 포춘 메시지를 포함해야 하는지 확인합니다.
     *
     * @param request  현재 HTTP 요청
     * @param response 현재 HTTP 응답
     * @return 포춘 메시지를 포함해야 하면 true, 아니면 false
     */
    private boolean shouldIncludeFortune(HttpServletRequest request,
                                         HttpServletResponse response) {
        // 전체 기능이 비활성화된 경우
        if (!fortuneCookieProperties.isEnabled()) {
            return false;
        }

        // URL 패턴 기반 제외 처리
        String requestPath = request.getRequestURI();
        if (fortuneCookieProperties.getExcludePatterns().stream()
                .anyMatch(pattern -> requestPath.matches(pattern))) {
            return false;
        }

        // 에러 응답 처리 (설정에 따라 에러 응답 제외)
        if (!fortuneCookieProperties.isIncludeOnError() &&
                response.getStatus() >= 400) {
            return false;
        }

        // 특정 상태 코드만 포함하도록 설정된 경우 체크
        if (!fortuneCookieProperties.getIncludedStatusCodes().isEmpty() &&
                !fortuneCookieProperties.getIncludedStatusCodes().contains(response.getStatus())) {
            return false;
        }

        return true;
    }

    /**
     * 현재 응답이 JSON 형식인지 확인합니다.
     *
     * @param response 현재 HTTP 응답
     * @return JSON 응답이면 true, 아니면 false
     */
    private boolean isJsonResponse(HttpServletResponse response) {
        String contentType = response.getContentType();
        return contentType != null &&
                contentType.toLowerCase().contains(MediaType.APPLICATION_JSON_VALUE);
    }

    /**
     * JSON 응답 본문에 포춘 메시지를 추가합니다.
     * 원본 JSON 객체의 구조를 유지하면서 새로운 필드로 포춘 메시지를 추가합니다.
     *
     * @param response 현재 HTTP 응답
     * @param fortune  추가할 포춘 메시지
     * @throws Exception JSON 처리 중 발생할 수 있는 예외
     */
    private void modifyJsonResponse(HttpServletResponse response, String fortune) throws Exception {
        // 응답 내용을 캐싱하여 수정 가능하게 만듦
        ContentCachingResponseWrapper wrapper =
                new ContentCachingResponseWrapper(response);

        // 원본 응답 내용을 문자열로 변환
        byte[] content = wrapper.getContentAsByteArray();
        String json = new String(content, StandardCharsets.UTF_8);

        // JSON 파싱 및 포춘 메시지 추가
        JsonNode root = objectMapper.readTree(json);
        if (root.isObject()) {
            ((ObjectNode) root).put(fortuneCookieProperties.getResponseFortuneName(), fortune);

            // 수정된 JSON을 바이트 배열로 변환
            byte[] modifiedContent =
                    objectMapper.writeValueAsString(root).getBytes(StandardCharsets.UTF_8);

            // 수정된 내용을 응답에 쓰기
            wrapper.setContentLength(modifiedContent.length);
            wrapper.getResponse().getOutputStream().write(modifiedContent);
        }

        // 최종 응답 복사
        wrapper.copyBodyToResponse();
    }

}