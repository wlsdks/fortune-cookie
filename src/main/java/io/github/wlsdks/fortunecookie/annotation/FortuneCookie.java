package io.github.wlsdks.fortunecookie.annotation;

import io.github.wlsdks.fortunecookie.properties.FortuneMode;
import io.github.wlsdks.fortunecookie.properties.GameType;

import java.lang.annotation.*;

/**
 * 포춘 쿠키 메시지를 자동으로 추가할 컨트롤러나 메서드에 붙이는 어노테이션입니다.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FortuneCookie {

    // 이 메서드에만 다른 gameType을 쓸 수 있도록 설정
    GameType gameType() default GameType.UNSPECIFIED;

    // 게임을 아예 비활성화 할 수 있도록 설정
    boolean gameEnabled() default true;

    // 다른 모드를 쓸 수 있도록 설정
    FortuneMode mode() default FortuneMode.UNSPECIFIED;

}
