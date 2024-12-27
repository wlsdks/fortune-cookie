package io.github.wlsdks.fortunecookie.interceptor.module;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 미니 게임 로직을 추상화하는 인터페이스.
 * 여러 종류의 게임(숫자 맞히기, 퀴즈 등)에 대해 공통 메서드 시그니처를 정의.
 */
public interface GameModule {

    /**
     * 게임 로직을 처리하고, 보완된 fortune 메시지를 반환한다.
     *
     * @param request        현재 요청 (헤더/세션 접근 가능)
     * @param currentFortune 현재까지 만들어진 포춘 메시지
     * @return 게임 결과가 반영된 새로운 메시지
     */
    String processGame(HttpServletRequest request, String currentFortune);

}