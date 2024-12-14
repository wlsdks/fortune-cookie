package io.github.wlsdks.fortunecookie;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wlsdks.fortunecookie.interceptor.FortuneCookieResponseAdvice;
import io.github.wlsdks.fortunecookie.properties.FortuneCookieProperties;
import io.github.wlsdks.fortunecookie.provider.FortuneProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FortuneCookieResponseAdviceTest {

    @Mock
    private FortuneProvider fortuneProvider;

    private FortuneCookieProperties properties;
    private FortuneCookieResponseAdvice advice;

    @BeforeEach
    void setUp() {
        properties = new FortuneCookieProperties();
        properties.setEnabled(true);
        properties.setIncludeHeader(true);
        properties.setHeaderName("X-Fortune-Cookie");
        properties.setIncludeInResponse(true);
        properties.setResponseFortuneName("fortune");

        advice = new FortuneCookieResponseAdvice(fortuneProvider, properties, new ObjectMapper());
    }

    @DisplayName("JSON 응답에 포춘 메시지가 정상적으로 추가된다")
    @Test
    void shouldAddFortuneMessageToResponse() {
        // given
        String expectedFortune = "행운이 찾아올 것입니다!";
        when(fortuneProvider.getFortune(any(Locale.class))).thenReturn(expectedFortune);

        Map<String, String> body = new HashMap<>();
        body.put("message", "Hello");

        ServerHttpRequest request = mock(ServerHttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        when(request.getHeaders()).thenReturn(headers);

        // when
        Object result = advice.beforeBodyWrite(body, null, MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class, request, null);

        // then
        assertThat(result).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        assertThat(resultMap)
                .containsEntry("message", "Hello")
                .containsEntry("fortune", expectedFortune);
    }

    /*@Test
    @DisplayName("fortune-cookie.enabled=false일 때는 원본 응답을 그대로 반환한다")
    void shouldNotModifyResponseWhenDisabled() {
        // given
        properties.setEnabled(false);
        Map<String, String> body = new HashMap<>();
        body.put("message", "Hello");

        // when
        Object result = advice.beforeBodyWrite(body, null, MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class, null, null);

        // then
        assertThat(result).isEqualTo(body);
    }*/

    @Test
    @DisplayName("non-Map 응답은 수정하지 않고 그대로 반환한다")
    void shouldNotModifyNonMapResponse() {
        // given
        String body = "Simple string response";

        // when
        Object result = advice.beforeBodyWrite(body, null, MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class, null, null);

        // then
        assertThat(result).isEqualTo(body);
    }
}