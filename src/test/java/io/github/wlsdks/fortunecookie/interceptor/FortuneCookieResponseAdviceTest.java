package io.github.wlsdks.fortunecookie.interceptor;

import io.github.wlsdks.fortunecookie.properties.FortuneCookieProperties;
import io.github.wlsdks.fortunecookie.provider.DefaultFortuneProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class FortuneCookieResponseAdviceTest {

    private DefaultFortuneProvider fortuneProvider;
    private FortuneCookieProperties properties;
    private FortuneCookieResponseAdvice advice;

    private MockHttpServletRequest servletRequest;
    private MockHttpServletResponse servletResponse;

    @BeforeEach
    public void setUp() {
        properties = new FortuneCookieProperties();
        properties.setEnabled(true);
        properties.setIncludeInResponse(true);
        properties.setResponseFortuneName("fortune");

        advice = new FortuneCookieResponseAdvice(fortuneProvider, properties);

        servletRequest = new MockHttpServletRequest();
        servletResponse = new MockHttpServletResponse();
    }

    @Test
    public void testBeforeBodyWriteWithMapBody() {
        String bodyFortune = "오늘은 행운이 가득한 날입니다!";
        servletRequest.setAttribute("fortuneBody", bodyFortune);

        Map<String, Object> body = new HashMap<>();
        body.put("message", "Hello World!!!");

        Object result = advice.beforeBodyWrite(
                body,
                mock(MethodParameter.class),
                MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class,
                new ServletServerHttpRequest(servletRequest),
                new ServletServerHttpResponse(servletResponse)
        );

        assertTrue(result instanceof Map);
        Map<String, Object> resultMap = (Map<String, Object>) result;
        assertEquals("Hello World!!!", resultMap.get("message"));
        assertEquals(bodyFortune, resultMap.get("fortune"));
    }

    @Test
    public void testBeforeBodyWriteWithNonMapBody() {
        String bodyFortune = "오늘은 행운이 가득한 날입니다!";
        servletRequest.setAttribute("fortuneBody", bodyFortune);

        String body = "Non-Map Body";

        Object result = advice.beforeBodyWrite(
                body,
                mock(MethodParameter.class),
                MediaType.TEXT_PLAIN,
                MappingJackson2HttpMessageConverter.class,
                new ServletServerHttpRequest(servletRequest),
                new ServletServerHttpResponse(servletResponse)
        );

        assertEquals(body, result);
    }

    @Test
    public void testBeforeBodyWriteWithIncludeInResponseFalse() {
        properties.setIncludeInResponse(false);

        String bodyFortune = "오늘은 행운이 가득한 날입니다!";
        servletRequest.setAttribute("fortuneBody", bodyFortune);

        Map<String, Object> body = new HashMap<>();
        body.put("message", "Hello World!!!");

        Object result = advice.beforeBodyWrite(
                body,
                mock(MethodParameter.class),
                MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class,
                new ServletServerHttpRequest(servletRequest),
                new ServletServerHttpResponse(servletResponse)
        );

        assertTrue(result instanceof Map);
        Map<String, Object> resultMap = (Map<String, Object>) result;
        assertEquals("Hello World!!!", resultMap.get("message"));
        assertNull(resultMap.get("fortune"));
    }

    @Test
    public void testSupportsMethod() {
        MethodParameter methodParameter = mock(MethodParameter.class);
        Class<? extends org.springframework.http.converter.HttpMessageConverter<?>> converterType = MappingJackson2HttpMessageConverter.class;

        assertTrue(advice.supports(methodParameter, converterType));

        // 다른 converter type
        Class<? extends org.springframework.http.converter.HttpMessageConverter<?>> otherConverter = org.springframework.http.converter.StringHttpMessageConverter.class;
        assertFalse(advice.supports(methodParameter, otherConverter));
    }
}
