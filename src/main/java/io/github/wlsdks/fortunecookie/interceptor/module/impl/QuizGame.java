package io.github.wlsdks.fortunecookie.interceptor.module.impl;

import io.github.wlsdks.fortunecookie.interceptor.module.GameModule;
import io.github.wlsdks.fortunecookie.properties.FortuneCookieProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.MessageSource;

import java.util.Random;

public class QuizGame implements GameModule {

    public static final String X_QUIZ_ANSWER = "X-Quiz-Answer";
    public static final String QUIZ_INDEX = "quizIndex";
    private final FortuneCookieProperties properties;
    private final MessageSource messageSource;
    private final Random random;

    public QuizGame(FortuneCookieProperties properties, MessageSource messageSource, Random random) {
        this.properties = properties;
        this.messageSource = messageSource;
        this.random = random;
    }

    private static final String[] QUESTIONS = {
            "What is the capital of France?",
            "2 + 2 = ?",
            "What's the largest planet in our Solar System?"
    };
    private static final String[] ANSWERS = {
            "Paris", "4", "Jupiter"
    };

    @Override
    public String processGame(HttpServletRequest request, String currentFortune) {
        HttpSession session = request.getSession();

        // 문제 인덱스를 저장한다.
        Object quizIndexObj = session.getAttribute(QUIZ_INDEX);
        int quizIndex;

        if (quizIndexObj == null) {
            quizIndex = random.nextInt(QUESTIONS.length);
            session.setAttribute(QUIZ_INDEX, quizIndex);
        } else {
            quizIndex = (int) quizIndexObj;
        }

        // 사용자가 X-Quiz-Answer 헤더로 답을 보냈는지 체크
        String userAnswer = request.getHeader(X_QUIZ_ANSWER);

        // 사용자가 답을 보냈을 경우
        if (userAnswerExist(userAnswer)) {
            // 정답인 경우
            if (isCollect(userAnswer, quizIndex)) {
                currentFortune += " " + messageSource.getMessage(
                        "game.quiz.correct",
                        new Object[]{ANSWERS[quizIndex]},
                        request.getLocale()
                );

                // 다음 라운드 문제를 위해 새로운 인덱스 생성
                int newIndex = random.nextInt(QUESTIONS.length);
                session.setAttribute(QUIZ_INDEX, newIndex);
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
            // 헤더가 없으면 현재 문제 출력
            currentFortune += " [Quiz] " + QUESTIONS[quizIndex];
            currentFortune += " " + messageSource.getMessage(
                    "game.quiz_prompt",
                    null,
                    request.getLocale()
            );
        }

        // 최종 포춘 반환
        return currentFortune;
    }

    private boolean userAnswerExist(String userAnswer) {
        return userAnswer != null;
    }

    private boolean userAnswerNotExist(String userAnswer) {
        return !userAnswerExist(userAnswer);
    }

    private boolean isCollect(String userAnswer, int quizIndex) {
        return userAnswer.trim().equalsIgnoreCase(ANSWERS[quizIndex]);
    }

    private boolean isNotCollect(String userAnswer, int quizIndex) {
        return !isCollect(userAnswer, quizIndex);
    }

}
