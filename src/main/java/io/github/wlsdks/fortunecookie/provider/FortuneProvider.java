package io.github.wlsdks.fortunecookie.provider;

import java.util.Locale;

/**
 * 포춘 메시지를 제공하는 인터페이스입니다.
 */
public interface FortuneProvider {

    /**
     * 포춘 키를 생성합니다.
     *
     * @return 포춘 메시지 키
     */
    String generateFortuneKey();

    /**
     * 포춘 메시지를 키와 로케일 기반으로 가져옵니다.
     *
     * @param fortuneKey 포춘 메시지 키
     * @param locale     포춘 메시지 로케일
     * @return 포춘 메시지
     */
    String getFortune(String fortuneKey, Locale locale);

}
