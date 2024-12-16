package io.github.wlsdks.fortunecookie.provider;

import io.github.wlsdks.fortunecookie.properties.FortuneCookieProperties;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Random;

/**
 * 포춘 메시지를 제공하는 기본 구현체입니다.
 * 포춘 메시지는 메시지 프로퍼티 파일에서 랜덤하게 가져옵니다.
 */
@Component
public class DefaultFortuneProvider implements FortuneProvider {

    private final MessageSource messageSource;
    private final FortuneCookieProperties properties;
    private final Random random;

    private static final String MESSAGE_PREFIX = "fortune."; // 메시지 프로퍼티 접두어

    public DefaultFortuneProvider(MessageSource messageSource, FortuneCookieProperties properties) {
        this.messageSource = messageSource;
        this.properties = properties;
        this.random = new Random();
    }

    /**
     * fortunes-count 설정값 범위 내에서 랜덤한 키를 생성합니다.
     *
     * @return 생성된 포춘 메시지 키 (예: "fortune.1", "fortune.2" 등)
     */
    public String generateFortuneKey() {
        double roll = random.nextDouble();

        // 1% 확률로 특별한 메시지 반환
        if (roll < 0.01) {
            return "fortune.special";
        }

        // 일자별로 다른 메시지 반환
        DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
        if (dayOfWeek == DayOfWeek.MONDAY) {
            // 월요일에는 "fortune.monday" 메시지 반환
            return "fortune.monday";
        }
        if (dayOfWeek == DayOfWeek.FRIDAY) {
            // 금요일에는 "fortune.friday" 메시지 반환
            return "fortune.friday";
        }

        // 일반 포춘: 1 ~ fortunesCount 범위 내에서 랜덤하게 선택
        int messageIndex = random.nextInt(properties.getFortunesCount()) + 1;
        return MESSAGE_PREFIX + messageIndex;
    }

    /**
     * 지정된 로케일에 맞는 포춘 메시지를 반환합니다.
     *
     * @param fortuneKey 포춘 메시지 키
     * @param locale     메시지를 가져올 로케일
     * @return 랜덤하게 선택된 포춘 메시지 또는 기본 메시지
     */
    @Override
    public String getFortune(String fortuneKey, Locale locale) {
        try {
            String message = messageSource.getMessage(fortuneKey, null, locale);
            if (message.contains("fortune")) {
                // fortune.1, fortune.2 등의 메시지가 없을 경우 기본 메시지 반환
                return messageSource.getMessage("fortune.default", null,
                        "Today is your lucky day!", locale);
            }
            return message;
        } catch (Exception e) {
            // 키가 없거나 다른 문제가 발생했을 때 기본 메시지 반환
            return messageSource.getMessage("fortune.default", null,
                    "Today is your lucky day!", locale);
        }
    }

}
