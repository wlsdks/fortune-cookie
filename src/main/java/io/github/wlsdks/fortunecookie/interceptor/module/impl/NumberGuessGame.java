package io.github.wlsdks.fortunecookie.interceptor.module.impl;

import io.github.wlsdks.fortunecookie.common.Constant;
import io.github.wlsdks.fortunecookie.interceptor.module.GameModule;
import io.github.wlsdks.fortunecookie.properties.FortuneCookieProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.MessageSource;

import java.util.Random;

public class NumberGuessGame implements GameModule {

    private final FortuneCookieProperties properties;
    private final MessageSource messageSource;
    private final Random random;

    public NumberGuessGame(FortuneCookieProperties properties, MessageSource messageSource, Random random) {
        this.properties = properties;
        this.messageSource = messageSource;
        this.random = random;
    }

    @Override
    public String processGame(HttpServletRequest request, String currentFortune) {
        // 1) 세션에서 secretNumber를 가져옴, 없으면 새로 생성
        HttpSession session = request.getSession();
        Object secretNumberObj = session.getAttribute(Constant.SECRET_NUMBER);

        int secretNumber;
        if (secretNumberObj == null) {
            secretNumber = random.nextInt(properties.getGameRange()) + 1;
            session.setAttribute(Constant.SECRET_NUMBER, secretNumber);
        } else {
            secretNumber = (int) secretNumberObj;
        }

        // 2) 클라이언트에서 X-Guess 헤더로 추측 값을 전달받음
        String guessHeader = request.getHeader(Constant.X_GUESS);
        if (guessHeader != null) {
            try {
                int guess = Integer.parseInt(guessHeader);
                if (guess == secretNumber) {
                    // 정답
                    String successMessage = messageSource.getMessage(
                            "game.guessed_correctly",
                            new Object[]{secretNumber},
                            request.getLocale()
                    );
                    currentFortune += " " + successMessage;
                    // 다음 라운드를 위해 새로운 숫자 생성
                    secretNumber = random.nextInt(properties.getGameRange()) + 1;
                    session.setAttribute("secretNumber", secretNumber);
                } else {
                    // 오답
                    String wrongGuessMessage = messageSource.getMessage(
                            "game.wrong_guess",
                            null,
                            request.getLocale()
                    );
                    currentFortune += " " + wrongGuessMessage;
                }
            } catch (NumberFormatException e) {
                // 잘못된 형식
                String invalidFormatMessage = messageSource.getMessage(
                        "game.invalid_guess",
                        null,
                        request.getLocale()
                );
                currentFortune += " " + invalidFormatMessage;
            }
        } else {
            // 추측 헤더가 없으면 안내 메시지
            String guessPromptMessage = messageSource.getMessage(
                    "game.guess_prompt",
                    new Object[]{properties.getGameRange()},
                    request.getLocale()
            );
            currentFortune += " " + guessPromptMessage;
        }

        return currentFortune;
    }

}
