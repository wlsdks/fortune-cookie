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
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;
import java.util.stream.Collectors;

import static io.github.wlsdks.fortunecookie.common.Constant.*;

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

        // 4. 헤더용 메시지는 항상 영어로 (한글 오류가 발생할 수 있음)
        String headerFortune = fortuneProvider.getFortune(fortuneKey, Locale.ENGLISH);

        // 5. 바디용 메시지는 요청 로케일로 가져옴
        String bodyFortune = fortuneProvider.getFortune(fortuneKey, request.getLocale());

        // 6. 헤더에 포춘 쿠키 추가 (placeHolder 치환 포함)
        headerFortune = applyPlaceHolders(headerFortune, request);
        if (properties.isIncludeHeader()) {
            response.setHeader(properties.getHeaderName(), headerFortune);
        }

        // 7. 바디 메시지에 placeHolder 적용
        bodyFortune = applyPlaceHolders(bodyFortune, request);

        // 8. 미니게임 적용: 게임 모듈이 활성화되어 있으면, 어노테이션에 적힌 gameType(number, quiz)을 우선 적용
        bodyFortune = applyMiniGame(request, annotation, bodyFortune);

        // 9. 완성된 최종 바디 메시지를 request에 저장 (ResponseBodyAdvice가 참조함)
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
        // 1. 핸들러가 HandlerMethod가 아니면 null 리턴
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return null;
        }

        // 2. 메서드 레벨 어노테이션 확인
        FortuneCookie annotation = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), FortuneCookie.class);

        // 3. 클래스 레벨 어노테이션 확인
        if (annotation == null) {
            annotation = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), FortuneCookie.class);
        }

        // 4. FortuneCookie 어노테이션 리턴
        return annotation;
    }

    /**
     * 어노테이션 또는 프로퍼티에 지정된 모드를 가져오는 메서드
     *
     * @param annotation : FortuneCookie 어노테이션
     * @return : 최종 모드
     */
    private FortuneMode getFortuneMode(FortuneCookie annotation) {
        // 만약 어노테이션에 명시되지 않았으면 프로퍼티 기본값 사용 (yml 등 설정)
        if (annotation.mode() == FortuneMode.UNSPECIFIED) {
            return properties.getMode();
        }

        // 어노테이션에 명시되어 있으면 그것을 사용
        return annotation.mode();
    }

    /**
     * applyPlaceHolders : 플레이스홀더 치환 메서드
     *
     * @param originalMessage : 원본 메시지
     * @param request         : 현재 요청
     * @return : 치환된 메시지
     */
    private String applyPlaceHolders(String originalMessage,
                                     HttpServletRequest request) {
        // 1. placeholderEnabled가 꺼져 있으면 그냥 원본 메시지 리턴
        if (!properties.isPlaceholderEnabled()) {
            return originalMessage;
        }

        // 2. 실제 치환 로직
        String result = originalMessage;
        for (Map.Entry<String, String> entry : properties.getPlaceholderMapping().entrySet()) {
            // 2-1. e.g. entry = "userName" : "header:X-User-Name" 에서 key, value를 가져옴
            String placeholderKey = entry.getKey();      // e.g. "userName"
            String mappingSpec = entry.getValue();       // e.g. "header:X-User-Name"
            String placeholderPattern = "{" + placeholderKey + "}";

            // 2-2. 메시지에 placeholder가 없으면 다음으로
            if (!result.contains(placeholderPattern)) {
                continue;
            }

            // 2-3. placeholder에 해당하는 값을 가져와서 치환
            String resolvedValue = resolvePlaceholderValue(mappingSpec, request);

            // 2-4. 치환된 값이 없으면 GUEST로 대체
            result = result.replace(placeholderPattern, Objects.requireNonNullElse(resolvedValue, Constant.GUEST));
        }

        // 3. 치환된 메시지 리턴
        return result;
    }

    /**
     * applyMiniGame : 게임 모듈을 적용하는 메서드 (숫자 맞히기, 퀴즈 등)
     *
     * @param request     : 현재 요청
     * @param annotation  : FortuneCookie 어노테이션
     * @param bodyFortune : 현재까지 만들어진 포춘 메시지
     * @return : 게임 결과가 반영된 새로운 메시지
     */
    private String applyMiniGame(HttpServletRequest request,
                                 FortuneCookie annotation,
                                 String bodyFortune) {
        // 1. 미니게임이 켜져있다면 실행
        if (properties.isGameEnabled()) {
            GameType finalGameType;

            // 2. 어노테이션에 gameType이 명시되어 있으면 그것을 사용, 아니면 프로퍼티 기본값 사용
            if (annotation.gameType() != GameType.UNSPECIFIED) {
                finalGameType = annotation.gameType();  // 사용자가 어노테이션에 지정한 값
            } else {
                finalGameType = properties.getGameType();  // 프로퍼티 기본값
            }

            // 3. 해당 게임 모듈을 가져옴
            GameModule gameModule = gameModuleMap.get(finalGameType.getType());

            // 4. 게임 모듈이 존재하면 실행
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
        // 1. mappingSpec이 없거나 비어 있으면 null 리턴
        if (mappingSpec == null || mappingSpec.isBlank()) {
            return null;
        }

        // 2. mappingSpec의 값을 :로 분리해서 sourceType, sourceKey로 저장
        String[] tokens = mappingSpec.split(Constant.COLON);
        if (tokens.length != 2) {
            return null; // 잘못된 형식이면 null 리턴 (e.g. "header:X-User-Name:extra")
        }

        String sourceType = tokens[0];  // "header", "session" 등
        String sourceKey = tokens[1];   // "X-User-Name", "USER_NAME"

        // 3. sourceType에 따라 값을 가져오기 (헤더, 세션)
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
            // 스프링 시큐리티를 사용하는 경우
            case SECURITY -> {
                try {
                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                    if (isAuthenticatedUser(auth)) {
                        // 유저 정보를 가져오는 경우
                        if (USERNAME.equals(sourceKey)) {
                            return auth.getName();
                        }
                        // 유저 역할을 가져오는 경우
                        if (ROLES.equals(sourceKey)) {
                            return auth.getAuthorities().stream()
                                    .map(GrantedAuthority::getAuthority)
                                    .collect(Collectors.joining(","));
                        }
                        // 유저 principal을 가져오는 경우
                        if (PRINCIPAL.equals(sourceKey)) {
                            Object principal = auth.getPrincipal();
                            return principal != null ? principal.toString() : null;
                        }
                    }
                } catch (Exception e) {
                    log.debug("Failed to access security context: {}", e.getMessage());
                    return null;
                }
                return null;
            }
            default -> {
                return null;
            }
        }
    }

    /**
     * 사용자가 인증되었는지 확인하는 메서드
     *
     * @param auth : 현재 사용자 인증 정보
     * @return : 인증 여부
     */
    private boolean isAuthenticatedUser(Authentication auth) {
        return auth != null && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);
    }

}
