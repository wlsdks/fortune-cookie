package io.github.wlsdks.fortunecookie.provider;

import io.github.wlsdks.fortunecookie.common.Constant;
import io.github.wlsdks.fortunecookie.properties.FortuneCookieProperties;
import io.github.wlsdks.fortunecookie.properties.FortuneMode;
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
        // 기본값 가져오기
        FortuneMode fortuneMode = properties.getMode();
        String prefix = Constant.MESSAGE_PREFIX; // 기본 prefix (fortune)

        // mode에 따라 prefix 변경
        if (FortuneMode.JOKE.equals(fortuneMode)) {
            prefix = Constant.JOKE_MESSAGE;
        }
        if (FortuneMode.QUOTE.equals(fortuneMode)) {
            prefix = Constant.QUOTE_MESSAGE;
        }

        // 0.0 <= roll < 1.0 범위의 랜덤한 double 값 생성
        double roll = random.nextDouble();

        // 1% 확률로 특별한 메시지 반환 (해당 모드에 맞춘 special 키 사용)
        if (roll < 0.01) {
            return Constant.SPECIAL_MESSAGE;
        }

        // 특수한 일자에는 다른 메시지 반환 (이 부분은 mode와 상관없이 특정 키 사용)
        DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
        if (dayOfWeek == DayOfWeek.MONDAY) {
            return Constant.MONDAY_MESSAGE;
        }
        if (dayOfWeek == DayOfWeek.FRIDAY) {
            return Constant.FRIDAY_MESSAGE;
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
        // 메시지 프로퍼티에서 포춘 메시지 가져오기
        String message = messageSource.getMessage(fortuneKey, null, locale);

        // 만약 메시지가 null이거나 비어있으면 기본 메시지로 대체
        if (message.equals(fortuneKey)) {
            return messageSource.getMessage(getDefaultKeyForCurrentMode(), null,
                    "오늘은 농담이 없습니다. X-Guess 헤더를 사용하여 1에서 20 사이의 숫자를 추측하세요!", locale);
        }

        // 포춘 메시지 반환
        return message;
    }


    /**
     * 현재 mode에 맞는 default 키를 반환하는 헬퍼 메서드
     */
    private String getDefaultKeyForCurrentMode() {
        FortuneMode fortuneMode = properties.getMode();

        // mode에 따라 다른 default 키를 반환
        if (FortuneMode.JOKE.equals(fortuneMode)) {
            return "fortune.joke.default";
        }
        if (FortuneMode.QUOTE.equals(fortuneMode)) {
            return "fortune.quote.default";
        }

        // 기본값 반환
        return "fortune.default";
    }

}
