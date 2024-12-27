package io.github.wlsdks.fortunecookie.interceptor;

import io.github.wlsdks.fortunecookie.annotation.FortuneCookie;
import io.github.wlsdks.fortunecookie.interceptor.module.GameModule;
import io.github.wlsdks.fortunecookie.interceptor.module.impl.NumberGuessGame;
import io.github.wlsdks.fortunecookie.interceptor.module.impl.QuizGame;
import io.github.wlsdks.fortunecookie.properties.FortuneCookieProperties;
import io.github.wlsdks.fortunecookie.properties.GameType;
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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 포춘 쿠키 메시지를 HTTP 응답 헤더에 추가하는 인터셉터입니다.
 */
@Slf4j
public class FortuneCookieInterceptor implements HandlerInterceptor {

    private final FortuneProvider fortuneProvider;
    private final FortuneCookieProperties properties;
    private final MessageSource messageSource;
    private final Map<String, GameModule> gameModuleMap;

    public FortuneCookieInterceptor(FortuneProvider fortuneProvider,
                                    FortuneCookieProperties properties,
                                    MessageSource messageSource,
                                    List<GameModule> gameModuleList) {
        this.fortuneProvider = fortuneProvider;
        this.properties = properties;
        this.messageSource = messageSource;
        this.gameModuleMap = new HashMap<>();

        // 게임 모듈을 맵에 넣어둠
        gameModuleList.forEach(gameModule -> {
            if (gameModule instanceof NumberGuessGame numberGuessGame) {
                gameModuleMap.put("number", numberGuessGame);
            }
            if (gameModule instanceof QuizGame quizGame) {
                gameModuleMap.put("quiz", quizGame);
            }
            // 추가적으로 게임 모듈을 넣을수 있음
        });
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
        // 1. 어노테이션이 없으면 인터셉터 비활성화
        if (hasFortuneCookieAnnotation(handler)) return true;
        // 2. 기능이 꺼져 있으면 인터셉터 비활성화
        if (!properties.isEnabled()) return true;

        // 3. 무작위 포춘 메시지 키 생성 (mode에 따른 키 선택은 FortuneProvider 내부 로직에 반영됨)
        String fortuneKey = fortuneProvider.generateFortuneKey();
        // 4. 헤더용 메시지 기존 로케일과 무관하게 항상 영어로 표시한다. (한국어를 사용하면 에러가 발생할 수 있음)
        String headerFortune = fortuneProvider.getFortune(fortuneKey, Locale.ENGLISH);
        // 5. 바디용 메시지 Request Locale로 포춘 메시지 생성
        String bodyFortune = fortuneProvider.getFortune(fortuneKey, request.getLocale());

        // 6. 헤더에 포춘 쿠키 추가
        headerFortune = applyPlaceHolders(headerFortune, request);
        if (properties.isIncludeHeader()) {
            response.setHeader(properties.getHeaderName(), headerFortune);
        }

        // 7. 바디 메시지에 placeHolder 적용
        bodyFortune = applyPlaceHolders(bodyFortune, request);

        // 8. 게임 모듈이 활성화되어 있으면 처리
        if (properties.isGameEnabled()) {
            // gameType: "number", "quiz", 등
            GameType gameType = properties.getGameType();
            GameModule gameModule = gameModuleMap.get(gameType.getType());

            // 게임 모듈이 있으면 처리
            if (gameModule != null) {
                bodyFortune = gameModule.processGame(request, bodyFortune);
            }
        }

        // 9. 최종 바디 메시지를 request.setAttribute("fortuneBody", bodyFortune)로 저장(나중에 ResponseBodyAdvice가 꺼내 씀
        request.setAttribute("fortuneBody", bodyFortune);

        // 10. 다음 인터셉터로 요청 전달 (컨트롤러 계속 진행)
        return true;
    }

    /**
     * FortuneCookie 어노테이션이 있는지 확인하는 메서드
     *
     * @param handler : 현재 요청 핸들러
     * @return : FortuneCookie 어노테이션이 없으면 true
     */
    private boolean hasFortuneCookieAnnotation(Object handler) {
        // 1. 어노테이션이 없는 경우 인터셉터 비활성화
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 2. WithFortuneCookie 어노테이션 확인
        FortuneCookie annotation = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), FortuneCookie.class);

        // 3. 클래스 레벨 어노테이션 확인
        if (annotation == null) {
            annotation = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), FortuneCookie.class);
        }

        // 4. 어노테이션이 없으면 포춘 쿠키 기능 비활성화
        return annotation == null;
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
