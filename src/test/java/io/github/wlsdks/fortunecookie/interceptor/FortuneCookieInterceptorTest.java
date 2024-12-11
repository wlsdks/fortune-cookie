package io.github.wlsdks.fortunecookie.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wlsdks.fortunecookie.properties.FortuneCookieProperties;
import io.github.wlsdks.fortunecookie.provider.FortuneProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class FortuneCookieResponseAdviceTest {

    @Mock
    private FortuneProvider fortuneProvider;

    private FortuneCookieProperties properties;
    private FortuneCookieResponseAdvice advice;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        properties = new FortuneCookieProperties();
        properties.setEnabled(true);
        properties.setIncludeHeader(true);
        properties.setHeaderName("X-Fortune-Cookie");
        properties.setIncludeInResponse(true);
        properties.setResponseFortuneName("fortune");

        objectMapper = new ObjectMapper();
        advice = new FortuneCookieResponseAdvice(fortuneProvider, properties, objectMapper);
    }

    @Test
    @DisplayName("ResponseBodyAdvice가 정상적으로 설정되었는지 확인")
    void supportsTest() {
        // given
        MethodParameter parameter = mock(MethodParameter.class);

        // when
        boolean result = advice.supports(parameter, MappingJackson2HttpMessageConverter.class);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("JSON 응답에 포춘 메시지가 정상적으로 추가된다")
    void shouldAddFortuneMessageToResponse() {
        // given
        String expectedFortune = "행운이 찾아올 것입니다!";
        given(fortuneProvider.getFortune(any(Locale.class))).willReturn(expectedFortune);

        ServerHttpRequest request = mock(ServerHttpRequest.class);
        ServerHttpResponse response = mock(ServerHttpResponse.class);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT_LANGUAGE, "ko-KR");

        given(request.getHeaders()).willReturn(headers);
        given(response.getHeaders()).willReturn(new HttpHeaders());

        Map<String, String> body = new HashMap<>();
        body.put("message", "Hello");

        // when
        Object result = advice.beforeBodyWrite(body, null, MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class, request, response);

        // then
        assertThat(result).isInstanceOf(Map.class);
        Map<String, Object> resultMap = (Map<String, Object>) result;

        assertThat(resultMap)
                .containsEntry("message", "Hello")
                .containsEntry("fortune", expectedFortune);
    }

    @Test
    @DisplayName("fortune-cookie.enabled=false일 때는 원본 응답을 그대로 반환한다")
    void shouldNotModifyResponseWhenDisabled() {
        // given
        properties.setEnabled(false);
        Map<String, String> body = new HashMap<>();
        body.put("message", "Hello");

        // when
        Object result = advice.beforeBodyWrite(body, null, MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class, null, null);  // request와 response를 null로 전달

        // then
        assertThat(result).isEqualTo(body);
    }

    @Test
    @DisplayName("non-Map 응답은 수정하지 않고 그대로 반환한다")
    void shouldNotModifyNonMapResponse() {
        // given
        String body = "Simple string response";

        ServerHttpRequest request = mock(ServerHttpRequest.class);
        ServerHttpResponse response = mock(ServerHttpResponse.class);
        given(request.getHeaders()).willReturn(new HttpHeaders());
        given(response.getHeaders()).willReturn(new HttpHeaders());

        // when
        Object result = advice.beforeBodyWrite(body, null, MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class, request, response);

        // then
        assertThat(result).isEqualTo(body);
    }
}