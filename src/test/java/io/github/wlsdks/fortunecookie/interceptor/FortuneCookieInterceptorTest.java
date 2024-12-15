package io.github.wlsdks.fortunecookie.interceptor;

import io.github.wlsdks.fortunecookie.properties.FortuneCookieProperties;
import io.github.wlsdks.fortunecookie.provider.FortuneProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class FortuneCookieInterceptorTest {

    private FortuneProvider fortuneProvider;
    private FortuneCookieProperties properties;
    private FortuneCookieInterceptor interceptor;

    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    public void setUp() {
        fortuneProvider = mock(FortuneProvider.class);
        properties = new FortuneCookieProperties();
        properties.setEnabled(true);
        properties.setIncludeHeader(true);
        properties.setHeaderName("X-Fortune-Cookie");
        properties.setIncludeInResponse(true);
        properties.setFortunesCount(50);

        interceptor = new FortuneCookieInterceptor(fortuneProvider, properties);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
    }

    @Test
    public void testPreHandleWithEnabledProperties() throws Exception {
        String fortuneKey = "fortune.1";
        String headerFortune = "Your hard work will pay off soon.";
        String bodyFortune = "오늘은 행운이 가득한 날입니다!";

        when(fortuneProvider.generateFortuneKey()).thenReturn(fortuneKey);
        when(fortuneProvider.getFortune(fortuneKey, Locale.ENGLISH)).thenReturn(headerFortune);
        when(request.getLocale()).thenReturn(Locale.KOREAN);
        when(fortuneProvider.getFortune(fortuneKey, Locale.KOREAN)).thenReturn(bodyFortune);

        boolean result = interceptor.preHandle(request, response, new Object());
        assertTrue(result);

        // Verify header is set
        verify(response, times(1)).setHeader("X-Fortune-Cookie", headerFortune);

        // Verify fortuneBody attribute is set
        verify(request, times(1)).setAttribute("fortuneBody", bodyFortune);
    }

    @Test
    public void testPreHandleWithDisabledProperties() throws Exception {
        properties.setEnabled(false);

        boolean result = interceptor.preHandle(request, response, new Object());
        assertTrue(result);

        // Verify no interactions
        verify(response, never()).setHeader(anyString(), anyString());
        verify(fortuneProvider, never()).generateFortuneKey();
        verify(request, never()).setAttribute(anyString(), any());
    }

//    @Test
//    public void testPreHandleWithIncludeHeaderFalse() throws Exception {
//        properties.setIncludeHeader(false);
//
//        String fortuneKey = "fortune.2";
//        String bodyFortune = "특별한 인연을 만나게 될 것입니다!";
//
//        when(fortuneProvider.generateFortuneKey()).thenReturn(fortuneKey);
//        when(fortuneProvider.getFortune(fortuneKey, Locale.KOREAN)).thenReturn(bodyFortune);
//
//        boolean result = interceptor.preHandle(request, response, new Object());
//        assertTrue(result);
//
//        // Verify header is not set
//        verify(response, never()).setHeader(anyString(), anyString());
//
//        // Verify fortuneBody attribute is set
//        verify(request, times(1)).setAttribute("fortuneBody", bodyFortune);
//    }
}
