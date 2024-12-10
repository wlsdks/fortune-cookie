package io.github.wlsdks.fortunecookie.provider;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Random;

@Component
public class DefaultFortuneProvider implements FortuneProvider {

    private final MessageSource messageSource;
    private final Random random;

    private static final String MESSAGE_PREFIX = "fortune."; // 메시지 프로퍼티 접두어
    private static final int FORTUNE_COUNT = 100;            // 포춘 메시지 개수

    public DefaultFortuneProvider(MessageSource messageSource, Random random) {
        this.messageSource = messageSource;
        this.random = random;
    }

    /**
     * 지정된 로케일에 맞는 포춘 메시지를 반환한다.
     *
     * @param locale locale 메시지를 가져올 로케일
     * @return 랜덤하게 선택된 포춘 메시지
     */
    @Override
    public String getFortune(Locale locale) {
        int messageIndex = random.nextInt(FORTUNE_COUNT) + 1;
        String messageKey = MESSAGE_PREFIX + messageIndex;

        try {
            return messageSource.getMessage(messageKey, null, locale);
        } catch (NoSuchMessageException e) {
            return messageSource.getMessage("fortune.default", null,
                    "Today is your lucky day!", locale);
        }
    }

}
