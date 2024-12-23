package io.github.wlsdks.fortunecookie.annotation;

import java.lang.annotation.*;

/**
 * 포춘 쿠키 메시지를 자동으로 추가할 컨트롤러나 메서드에 붙이는 어노테이션입니다.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FortuneCookie {
}
