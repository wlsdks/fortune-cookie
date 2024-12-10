package io.github.wlsdks.fortunecookie.provider;

import java.util.Locale;

public interface FortuneProvider {

    /**
     * 지정된 로케일에 맞는 포춘 메시지를 반환한다.
     *
     * @param locale locale 메시지를 가져올 로케일
     * @return 랜덤하게 선택된 포춘 메시지
     */
    String getFortune(Locale locale);

    /**
     * 기본 로케일을 사용하여 포춘 메시지를 반환한다.
     *
     * @return 랜덤하게 선택된 포춘 메시지
     */
    default String getFortune() {
        return getFortune(Locale.getDefault());
    }

}
