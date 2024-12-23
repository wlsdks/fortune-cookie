package io.github.wlsdks.fortunecookie.interceptor;

import io.github.wlsdks.fortunecookie.annotation.FortuneCookie;
import io.github.wlsdks.fortunecookie.properties.FortuneCookieProperties;
import io.github.wlsdks.fortunecookie.provider.FortuneProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * 포춘 쿠키 메시지를 HTTP 응답 헤더에 추가하는 인터셉터입니다.
 */
@Slf4j
public class FortuneCookieInterceptor implements HandlerInterceptor {

    private final FortuneProvider fortuneProvider;
    private final FortuneCookieProperties properties;
    private final Random random;
    private final MessageSource messageSource;

    public FortuneCookieInterceptor(FortuneProvider fortuneProvider,
                                    FortuneCookieProperties properties,
                                    MessageSource messageSource) {
        this.fortuneProvider = fortuneProvider;
        this.properties = properties;
        this.random = new Random();
        this.messageSource = messageSource;
    }

    /**
     * preHandle : 컨트롤러 실행 전에 호출
     * - 헤더는 영어로 고정
     * - 바디는 요청 로케일에 맞춤
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        // 1. 어노테이션이 없는 경우 인터셉터 비활성화
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 2. WithFortuneCookie 어노테이션 확인
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        FortuneCookie annotation = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), FortuneCookie.class);

        // 3. 클래스 레벨 어노테이션 확인
        if (annotation == null) {
            annotation = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), FortuneCookie.class);
        }

        // 4. 어노테이션이 없으면 포춘 쿠키 기능 비활성화
        if (annotation == null) {
            return true;
        }

        // 5. 기능 비활성화이면 그냥 통과
        if (!properties.isEnabled()) {
            return true;
        }

        // 6. 무작위 포춘 메시지 키 생성 (mode에 따른 키 선택은 FortuneProvider 내부 로직에 반영됨)
        String fortuneKey = fortuneProvider.generateFortuneKey();

        // 7. 헤더용 메시지 기존 로케일과 무관하게 항상 영어로 표시한다. (한국어를 사용하면 에러가 발생할 수 있음)
        String headerFortune = fortuneProvider.getFortune(fortuneKey, Locale.ENGLISH);
        headerFortune = applyPlaceHolders(headerFortune, request);

        // 8. 헤더에 포춘 쿠키 추가
        if (properties.isIncludeHeader()) {
            response.setHeader(properties.getHeaderName(), headerFortune);
        }

        // 9. 바디용 메시지 Request Locale로 포춘 메시지 생성
        String bodyFortune = fortuneProvider.getFortune(fortuneKey, request.getLocale());
        bodyFortune = applyPlaceHolders(bodyFortune, request);

        // 10. 미니게임 기능
        if (properties.isGameEnabled()) {
            // 세션에서 secretNumber를 가져옴, 없으면 새로 생성
            HttpSession session = request.getSession();
            Object secretNumberObj = session.getAttribute("secretNumber");
            int secretNumber;
            if (secretNumberObj == null) {
                secretNumber = random.nextInt(properties.getGameRange()) + 1;
                session.setAttribute("secretNumber", secretNumber);
            } else {
                secretNumber = (int) secretNumberObj;
            }

            // 클라이언트에서 X-Guess 헤더로 추측 값을 전달받음
            String guessHeader = request.getHeader("X-Guess");
            if (guessHeader != null) {
                try {
                    int guess = Integer.parseInt(guessHeader);
                    if (guess == secretNumber) {
                        String successMessage = messageSource.getMessage(
                                "game.guessed_correctly",
                                new Object[]{secretNumber},
                                request.getLocale()
                        );
                        bodyFortune += " " + successMessage;
                        // 다음 라운드를 위해 새로운 숫자 생성
                        secretNumber = random.nextInt(properties.getGameRange()) + 1;
                        session.setAttribute("secretNumber", secretNumber);
                    } else {
                        String wrongGuessMessage = messageSource.getMessage(
                                "game.wrong_guess",
                                null,
                                request.getLocale()
                        );
                        bodyFortune += " " + wrongGuessMessage;
                    }
                } catch (NumberFormatException e) {
                    String invalidFormatMessage = messageSource.getMessage(
                            "game.invalid_guess",
                            null,
                            request.getLocale()
                    );
                    bodyFortune += " " + invalidFormatMessage;
                }
            } else {
                // 추측 헤더가 없으면 게임 안내 메시지
                String guessPromptMessage = messageSource.getMessage(
                        "game.guess_prompt",
                        new Object[]{properties.getGameRange()},
                        request.getLocale()
                );
                bodyFortune += " " + guessPromptMessage;
            }
        }

        // Request Attribute에 저장 (ResponseBodyAdvice에서 사용)
        request.setAttribute("fortuneBody", bodyFortune);

        return true; // 컨트롤러 계속 진행
    }

    /**
     * postHandle : 컨트롤러 실행 후, View 렌더링 직전에 호출
     * - 현재 필요 없음
     */
    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) throws Exception {
        // 헤더 설정은 preHandle에서 이미 했으므로 별도 작업 없음
    }

    /**
     * afterCompletion : 모든 요청 처리가 끝난 후 호출
     * - 응답이 이미 커밋된 상태일 수 있어서 헤더 추가가 무효화될 확률이 큼
     */
    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) throws Exception {
        // 헤더 설정은 preHandle에서 이미 했으므로 별도 작업 없음
    }

    /**
     * 플레이스홀더 치환 메서드
     */
    private String applyPlaceHolders(String originalMessage, HttpServletRequest request) {
        // placeholderEnabled가 꺼져 있으면 그냥 원본 메시지 리턴
        if (!properties.isPlaceholderEnabled()) {
            return originalMessage;
        }

        // 실제 치환 로직
        String result = originalMessage;
        for (Map.Entry<String, String> entry : properties.getPlaceholderMapping().entrySet()) {
            String placeholderKey = entry.getKey();      // e.g. "userName"
            String mappingSpec = entry.getValue();       // e.g. "header:X-User-Name"
            String placeholderPattern = "{" + placeholderKey + "}";

            // 실제로 원본 메시지에 {userName} 같은 플레이스홀더가 없으면 스킵
            if (!result.contains(placeholderPattern)) {
                continue;
            }

            // mappingSpec을 파싱해서 값을 가져옴
            String resolvedValue = resolvePlaceholderValue(mappingSpec, request);

            // 치환
            if (resolvedValue != null) {
                result = result.replace(placeholderPattern, resolvedValue);
            } else {
                // 못 찾았으면 "Guest" 등 기본값 혹은 그대로 둠
                result = result.replace(placeholderPattern, "Guest");
            }
        }

        return result;
    }

    /**
     * mappingSpec을 분석해 헤더나 세션에서 값을 가져오는 메서드
     * 예: "header:X-User-Name" → request.getHeader("X-User-Name")
     * "session:USER_NAME"   → request.getSession().getAttribute("USER_NAME")
     */
    private String resolvePlaceholderValue(String mappingSpec, HttpServletRequest request) {
        if (mappingSpec == null || mappingSpec.isBlank()) {
            return null;
        }

        // 예: "header:X-User-Name"
        String[] tokens = mappingSpec.split(":");
        if (tokens.length != 2) {
            return null;
        }

        String sourceType = tokens[0];  // "header", "session", "security" 등
        String sourceKey = tokens[1];   // "X-User-Name", "USER_NAME"

        switch (sourceType) {
            case "header":
                return request.getHeader(sourceKey);
            case "session":
                HttpSession session = request.getSession(false);
                if (session != null) {
                    Object val = session.getAttribute(sourceKey);
                    return val != null ? val.toString() : null;
                }
                return null;
            default:
                return null;
        }
    }

}
