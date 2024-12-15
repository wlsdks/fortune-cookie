package io.github.wlsdks.fortunecookie.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@EnableConfigurationProperties(FortuneCookieProperties.class)
@TestPropertySource(properties = {
        "fortune-cookie.enabled=true",
        "fortune-cookie.include-header=true",
        "fortune-cookie.header-name=X-Fortune-Cookie",
        "fortune-cookie.include-in-response=true",
        "fortune-cookie.response-fortune-name=fortune",
        "fortune-cookie.fortunes-count=50",
        "fortune-cookie.debug=true"
})
public class FortuneCookiePropertiesTest {

    @Autowired
    private FortuneCookieProperties properties;

    @Test
    public void testPropertiesBinding() {
        assertTrue(properties.isEnabled());
        assertTrue(properties.isIncludeHeader());
        assertEquals("X-Fortune-Cookie", properties.getHeaderName());
        assertTrue(properties.isIncludeInResponse());
        assertEquals("fortune", properties.getResponseFortuneName());
        assertEquals(50, properties.getFortunesCount());
        assertTrue(properties.isDebug());

        // 기본값 테스트
        assertTrue(properties.getIncludedStatusCodes().isEmpty());
        assertTrue(properties.getExcludePatterns().isEmpty());
        assertTrue(properties.isIncludeOnError());
        assertEquals(0, properties.getMaxFortuneLength());
        assertEquals("", properties.getCustomMessagesPath());
    }

}
