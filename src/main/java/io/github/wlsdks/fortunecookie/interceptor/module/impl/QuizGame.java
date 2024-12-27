package io.github.wlsdks.fortunecookie.interceptor.module.impl;

import io.github.wlsdks.fortunecookie.common.Constant;
import io.github.wlsdks.fortunecookie.common.QuizConstant;
import io.github.wlsdks.fortunecookie.interceptor.module.GameModule;
import io.github.wlsdks.fortunecookie.properties.FortuneCookieProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.MessageSource;

import java.util.Random;

public class QuizGame implements GameModule {

    private final FortuneCookieProperties properties;
    private final MessageSource messageSource;
    private final Random random;

    public QuizGame(FortuneCookieProperties properties, MessageSource messageSource, Random random) {
        this.properties = properties;
        this.messageSource = messageSource;
        this.random = random;
    }


    /**
     * 게임 로직을 처리합니다.
     *
     * @param request        현재 요청 (헤더/세션 접근 가능)
     * @param currentFortune 현재까지 만들어진 포춘 메시지
     * @return 게임 결과가 반영된 새로운 메시지
     */
    @Override
    public String processGame(HttpServletRequest request, String currentFortune) {
        // 세션에서 QUIZ_INDEX 가져오기
        HttpSession session = request.getSession();
        int quizIndex = getOrCreateQuizIndex(session);

        // 사용자가 X-Quiz-Answer 헤더로 답을 보냈는지 체크
        String userAnswer = request.getHeader(Constant.X_QUIZ_ANSWER);

        // 사용자가 답을 보냈을 경우
        if (userAnswerExist(userAnswer)) {
            // 정답인 경우
            if (isCollect(userAnswer, quizIndex)) {
                currentFortune += " " + messageSource.getMessage(
                        "game.quiz.correct",
                        new Object[]{QuizConstant.ANSWERS[quizIndex]},
                        request.getLocale()
                );
                // 새로운 문제를 위해 인덱스 갱신
                int newIndex = random.nextInt(QuizConstant.QUESTIONS.length);
                session.setAttribute(Constant.QUIZ_INDEX, newIndex);
            }

            // 오답인 경우
            if (isNotCollect(userAnswer, quizIndex)) {
                currentFortune += " " + messageSource.getMessage(
                        "game.quiz.wrong",
                        null,
                        request.getLocale()
                );
            }
        }

        // 사용자가 답을 보내지 않았을 경우
        if (userAnswerNotExist(userAnswer)) {
            // 퀴즈 문제 출력
            String quizDisplay = messageSource.getMessage(
                    "game.quiz.display",
                    new Object[]{QuizConstant.QUESTIONS[quizIndex]},
                    request.getLocale()
            );

            // 현재 포춘에 퀴즈 문제 추가
            currentFortune += " " + quizDisplay;

            // 퀴즈 답안 요청 메시지
            currentFortune += " " + messageSource.getMessage(
                    "game.quiz_prompt",
                    null,
                    request.getLocale()
            );
        }

        // 최종 포춘 반환
        return currentFortune;
    }

    /**
     * 현재 세션에 저장된 QUIZ_INDEX가 없으면 새로 만들고, 있으면 그 값을 사용합니다.
     */
    private int getOrCreateQuizIndex(HttpSession session) {
        // 1. 세션에서 QUIZ_INDEX 가져오기
        Object quizIndexObj = session.getAttribute(Constant.QUIZ_INDEX);

        // 2. 세션에 저장된 QUIZ_INDEX가 없으면 새로 만들기
        if (quizIndexObj == null) {
            int newIndex = random.nextInt(QuizConstant.QUESTIONS.length);
            session.setAttribute(Constant.QUIZ_INDEX, newIndex);
            return newIndex;
        }

        // 3. 세션에 저장된 QUIZ_INDEX가 있으면 그 값을 사용
        return (int) quizIndexObj;
    }

    private boolean userAnswerExist(String userAnswer) {
        return userAnswer != null;
    }

    private boolean userAnswerNotExist(String userAnswer) {
        return !userAnswerExist(userAnswer);
    }

    private boolean isCollect(String userAnswer, int quizIndex) {
        return userAnswer.trim().equalsIgnoreCase(QuizConstant.ANSWERS[quizIndex]);
    }

    private boolean isNotCollect(String userAnswer, int quizIndex) {
        return !isCollect(userAnswer, quizIndex);
    }

}
