package io.github.wlsdks.fortunecookie.provider;

import io.github.wlsdks.fortunecookie.properties.FortuneCookieProperties;
import org.springframework.context.MessageSource;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Random;

/**
 * 포춘 메시지를 제공하는 기본 구현체입니다.
 * 포춘 메시지는 메시지 프로퍼티 파일에서 랜덤하게 가져옵니다.
 */
public class DefaultFortuneProvider implements FortuneProvider {

    private final MessageSource messageSource;
    private final FortuneCookieProperties properties;
    private final Random random;

    private static final String MESSAGE_PREFIX = "fortune"; // 메시지 프로퍼티 접두어

    public DefaultFortuneProvider(MessageSource messageSource, FortuneCookieProperties properties) {
        this.messageSource = messageSource;
        this.properties = properties;
        this.random = new Random();
    }

    /**
     * fortunes-count 설정값 범위 내에서 랜덤한 키를 생성합니다.
     *
     * @return 생성된 포춘 메시지 키 (예: "fortune.joke.1", "fortune.joke.2" 등)
     */
    public String generateFortuneKey() {
        // mode에 따라 prefix 결정
        // fortune(기본) -> fortune.*
        // joke 모드 -> fortune.joke.*
        // quote 모드 -> fortune.quote.*
        String mode = properties.getMode();
        String prefix = "fortune";

        if ("joke".equalsIgnoreCase(mode)) {
            prefix = "fortune.joke";
        } else if ("quote".equalsIgnoreCase(mode)) {
            prefix = "fortune.quote";
        }

        // 0.0 <= roll < 1.0 범위의 랜덤한 double 값 생성
        double roll = random.nextDouble();

        // 1% 확률로 특별한 메시지 반환 (해당 모드에 맞춘 special 키 사용)
        if (roll < 0.01) {
            return MESSAGE_PREFIX + ".special";
        }

        // 일자별로 다른 메시지 반환 (이 부분은 mode와 상관없이 특정 키 사용)
        // 만약 모드별 월요일/금요일 메시지를 다르게 하고 싶다면 prefix + ".monday" 식으로도 가능
        DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
        if (dayOfWeek == DayOfWeek.MONDAY) {
            return MESSAGE_PREFIX + ".monday";
        }
        if (dayOfWeek == DayOfWeek.FRIDAY) {
            return MESSAGE_PREFIX + ".friday";
        }

        // 일반 포춘: 1 ~ fortunesCount 범위 내에서 랜덤하게 선택
        int messageIndex = random.nextInt(properties.getFortunesCount()) + 1;
        return prefix + "." + messageIndex; // 수정: prefix 사용
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
        String message = messageSource.getMessage(fortuneKey, null, locale);
        if (message.equals(fortuneKey)) {
            // 키가 존재하지 않으면 기본 메시지 반환
            return messageSource.getMessage(getDefaultKeyForCurrentMode(), null,
                    "오늘은 농담이 없습니다. X-Guess 헤더를 사용하여 1에서 20 사이의 숫자를 추측하세요!", locale);
        }
        return message;
    }


    /**
     * 현재 mode에 맞는 default 키를 반환하는 헬퍼 메서드
     */
    private String getDefaultKeyForCurrentMode() {
        String mode = properties.getMode();

        // mode에 따라 다른 default 키를 반환
        if ("joke".equalsIgnoreCase(mode)) {
            return "fortune.joke.default";
        } else if ("quote".equalsIgnoreCase(mode)) {
            return "fortune.quote.default";
        }

        return "fortune.default"; // 기본 fortune 모드
    }

}
