package io.github.wlsdks.fortunecookie.interceptor;

import io.github.wlsdks.fortunecookie.annotation.FortuneCookie;
import io.github.wlsdks.fortunecookie.common.Constant;
import io.github.wlsdks.fortunecookie.interceptor.module.GameModule;
import io.github.wlsdks.fortunecookie.interceptor.module.impl.NumberGuessGame;
import io.github.wlsdks.fortunecookie.interceptor.module.impl.QuizGame;
import io.github.wlsdks.fortunecookie.properties.FortuneCookieProperties;
import io.github.wlsdks.fortunecookie.properties.FortuneMode;
import io.github.wlsdks.fortunecookie.properties.GameType;
import io.github.wlsdks.fortunecookie.provider.FortuneProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

/**
 * 포춘 쿠키 메시지를 HTTP 응답 헤더에 추가하는 인터셉터입니다.
 */
@Slf4j
public class FortuneCookieInterceptor implements HandlerInterceptor {

    private final FortuneProvider fortuneProvider;
    private final FortuneCookieProperties properties;
    private final Map<String, GameModule> gameModuleMap;

    public FortuneCookieInterceptor(FortuneProvider fortuneProvider,
                                    FortuneCookieProperties properties,
                                    List<GameModule> gameModuleList) {
        this.fortuneProvider = fortuneProvider;
        this.properties = properties;
        this.gameModuleMap = new HashMap<>();

        // 게임 모듈을 맵에 넣어둠
        gameModuleList.forEach(gameModule -> {
            if (gameModule instanceof NumberGuessGame numberGuessGame) {
                gameModuleMap.put("number", numberGuessGame);
            }
            if (gameModule instanceof QuizGame quizGame) {
                gameModuleMap.put("quiz", quizGame);
            }
            // 필요한 경우 다른 게임 모듈 추가 가능
        });
    }

    /**
     * preHandle : 컨트롤러 실행 전에 호출
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @param handler  chosen handler to execute, for type and/or instance evaluation
     * @return 만약 false를 반환하면, 요청을 중단하고 다음 인터셉터와 컨트롤러를 실행하지 않음
     * @throws Exception in case of errors
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {

        // 1. 어노테이션을 찾는다 (메서드 → 클래스 순)
        FortuneCookie annotation = getFortuneCookieAnnotation(handler);

        // 1-1. 어노테이션이 없으면 인터셉터 비활성화
        if (annotation == null) {
            return true; // 그냥 다음으로 진행
        }

        // 2. 기능이 꺼져 있으면 인터셉터 비활성화
        if (!properties.isEnabled()) {
            return true;
        }

        // 3) 만약 annotation이 없거나, annotation.mode()가 UNSPECIFIED면 (properties.getMode()를 쓰고, 아니면 어노테이션의 mode)
        FortuneMode finalMode = getFortuneMode(annotation);
        String fortuneKey = fortuneProvider.generateFortuneKey(finalMode);

        // 4. 헤더용 메시지는 항상 영어로
        String headerFortune = fortuneProvider.getFortune(fortuneKey, Locale.ENGLISH);

        // 5. 바디용 메시지는 요청 로케일
        String bodyFortune = fortuneProvider.getFortune(fortuneKey, request.getLocale());

        // 6. 헤더에 포춘 쿠키 추가(플레이스홀더 치환 포함)
        headerFortune = applyPlaceHolders(headerFortune, request);
        if (properties.isIncludeHeader()) {
            response.setHeader(properties.getHeaderName(), headerFortune);
        }

        // 7. 바디 메시지에 placeHolder 적용
        bodyFortune = applyPlaceHolders(bodyFortune, request);

        // 8. 게임 모듈이 활성화되어 있으면, 어노테이션에 적힌 gameType을 우선 적용
        bodyFortune = applyGame(request, annotation, bodyFortune);

        // 9. 최종 바디 메시지를 request에 저장 (ResponseBodyAdvice가 참조함)
        request.setAttribute(Constant.FORTUNE_BODY, bodyFortune);

        // 10. 다음 인터셉터 혹은 컨트롤러로 진행
        return true;
    }

    /**
     * postHandle : 컨트롤러 실행 후, View 렌더링 직전에 호출 (지금은 뷰 없음)
     *
     * @param request      current HTTP request
     * @param response     current HTTP response
     * @param handler      the handler (or {@link HandlerMethod}) that started asynchronous
     *                     execution, for type and/or instance examination
     * @param modelAndView the {@code ModelAndView} that the handler returned
     *                     (can also be {@code null})
     * @throws Exception in case of errors
     */
    @Override
    public void postHandle(@NonNull HttpServletRequest request,
                           @NonNull HttpServletResponse response,
                           @NonNull Object handler,
                           ModelAndView modelAndView) throws Exception {
        // 헤더 설정은 preHandle에서 이미 했으므로 별도 작업 없음
    }

    /**
     * afterCompletion : 모든 요청 처리가 끝난 후 호출 (응답이 이미 커밋된 상태일 수 있어서 헤더 추가가 무효화될 확률이 큼)
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @param handler  the handler (or {@link HandlerMethod}) that started asynchronous
     *                 execution, for type and/or instance examination
     * @param ex       any exception thrown on handler execution, if any; this does not
     *                 include exceptions that have been handled through an exception resolver
     * @throws Exception in case of errors
     */
    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) throws Exception {
        // 헤더 설정은 preHandle에서 이미 했으므로 별도 작업 없음
    }

    /**
     * 주어진 핸들러(메서드 또는 클래스)에 @FortuneCookie 어노테이션이 있는지 확인하는 메서드 (메서드 → 클래스 순)
     *
     * @param handler : 현재 핸들러
     * @return : FortuneCookie 어노테이션 또는 null
     */
    private FortuneCookie getFortuneCookieAnnotation(Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return null;
        }

        // 1) 메서드 레벨 어노테이션 확인
        FortuneCookie annotation = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), FortuneCookie.class);

        // 2) 클래스 레벨 어노테이션 확인
        if (annotation == null) {
            annotation = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), FortuneCookie.class);
        }

        return annotation;
    }

    /**
     * 어노테이션 또는 프로퍼티에 지정된 모드를 가져오는 메서드
     *
     * @param annotation : FortuneCookie 어노테이션
     * @return : 최종 모드
     */
    private FortuneMode getFortuneMode(FortuneCookie annotation) {
        FortuneMode finalMode;

        if (annotation.mode() == FortuneMode.UNSPECIFIED) {
            finalMode = properties.getMode(); // yml 등 설정
        } else {
            finalMode = annotation.mode();    // 어노테이션에 지정된 모드
        }

        return finalMode;
    }

    /**
     * applyPlaceHolders : 플레이스홀더 치환 메서드
     *
     * @param originalMessage : 원본 메시지
     * @param request         : 현재 요청
     * @return : 치환된 메시지
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

            if (!result.contains(placeholderPattern)) {
                continue;
            }

            String resolvedValue = resolvePlaceholderValue(mappingSpec, request);
            result = result.replace(placeholderPattern, Objects.requireNonNullElse(resolvedValue, Constant.GUEST));
        }
        return result;
    }

    /**
     * applyGame : 게임 모듈을 적용하는 메서드
     *
     * @param request     : 현재 요청
     * @param annotation  : FortuneCookie 어노테이션
     * @param bodyFortune : 현재까지 만들어진 포춘 메시지
     * @return : 게임 결과가 반영된 새로운 메시지
     */
    private String applyGame(HttpServletRequest request,
                             FortuneCookie annotation,
                             String bodyFortune) {
        //    어노테이션이 없으면 properties.getGameType() 사용
        if (properties.isGameEnabled()) {
            GameType finalGameType;

            // 어노테이션에 gameType이 명시되어 있으면 그것을 사용, 아니면 프로퍼티 기본값 사용
            if (annotation.gameType() != GameType.UNSPECIFIED) {
                finalGameType = annotation.gameType();  // 사용자가 어노테이션에 지정한 값
            } else {
                finalGameType = properties.getGameType();  // 프로퍼티 기본값
            }

            // 해당 게임 모듈을 찾아서 실행
            GameModule gameModule = gameModuleMap.get(finalGameType.getType());
            if (gameModule != null) {
                bodyFortune = gameModule.processGame(request, bodyFortune);
            }
        }

        // 게임 기능이 꺼져 있으면 그냥 원본 메시지 리턴
        return bodyFortune;
    }

    /**
     * mappingSpec을 분석해 헤더나 세션에서 값을 가져오는 메서드
     *
     * @param mappingSpec "header:X-User-Name" 또는 "session:USER_NAME" 등
     * @param request     현재 요청
     * @return 치환할 값
     */
    private String resolvePlaceholderValue(String mappingSpec, HttpServletRequest request) {
        if (mappingSpec == null || mappingSpec.isBlank()) {
            return null;
        }

        String[] tokens = mappingSpec.split(Constant.REGEX);
        if (tokens.length != 2) {
            return null;
        }

        String sourceType = tokens[0];  // "header", "session" 등
        String sourceKey = tokens[1];   // "X-User-Name", "USER_NAME"

        switch (sourceType) {
            case Constant.HEADER -> {
                return request.getHeader(sourceKey);
            }
            case Constant.SESSION -> {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    Object val = session.getAttribute(sourceKey);
                    return val != null ? val.toString() : null;
                }
                return null;
            }
            default -> {
                return null;
            }
        }
    }

}
