# Spring Fortune Cookie Library

Spring 애플리케이션에서 HTTP 응답에 자동으로 포춘 메시지를 추가해주는 라이브러리입니다.

## 기능
- HTTP 응답에 자동으로 포춘(운세) 메시지 추가
- JSON 응답 본문 또는 HTTP 헤더를 통한 메시지 전달
- 다국어(i18n) 지원
- 상세한 설정 옵션 제공
- Spring Boot Auto Configuration 지원

## 시작하기

### 의존성 추가
Maven:
```xml
<dependency>
    <groupId>io.github.wlsdks</groupId>
    <artifactId>spring-fortune-cookie</artifactId>
    <version>0.1.0</version>
</dependency>
```

Gradle:
```gradle
implementation 'io.github.wlsdks:spring-fortune-cookie:0.1.0'
```

### 기본 설정
`application.yml` 또는 `application.properties`에 다음과 같이 설정을 추가합니다:

```yaml
fortune-cookie:
  enabled: true                    # 기능 활성화 여부
  include-header: true            # HTTP 헤더에 포춘 메시지 포함
  header-name: X-Fortune-Cookie   # 포춘 메시지를 포함할 헤더 이름
  include-in-response: true       # JSON 응답에 포춘 메시지 포함
  response-fortune-name: fortune  # JSON 응답에 포함될 필드 이름
  debug: false                    # 디버그 모드 활성화 여부
  max-fortune-length: 0          # 포춘 메시지 최대 길이 (0 = 무제한)
  exclude-patterns:              # 포춘 메시지를 제외할 URL 패턴들
    - "/api/health.*"
    - "/actuator.*"
```

### 사용 예시

설정만 추가하면 자동으로 모든 JSON 응답에 포춘 메시지가 추가됩니다.

원본 응답:
```json
{
    "message": "Hello, World!"
}
```

포춘 메시지가 추가된 응답:
```json
{
    "message": "Hello, World!",
    "fortune": "오늘은 행운이 가득한 날입니다!"
}
```

HTTP 헤더를 통해서도 포춘 메시지를 확인할 수 있습니다:
```
X-Fortune-Cookie: 오늘은 행운이 가득한 날입니다!
```

## 커스터마이징

### 커스텀 포춘 메시지
`src/main/resources/fortunes/` 디렉토리에 다음 파일들을 추가하여 메시지를 커스터마이징할 수 있습니다:

```properties
# fortunes_ko.properties
fortune.1=오늘은 행운이 가득한 날입니다!
fortune.2=작은 시도가 큰 변화를 만들어낼 것입니다.
fortune.3=긍정적인 마음가짐이 좋은 결과를 가져올 것입니다.

# fortunes_en.properties
fortune.1=Today is your lucky day!
fortune.2=A small attempt will lead to a big change.
fortune.3=A positive mindset will bring positive results.
```

### FortuneProvider 커스터마이징
자신만의 포춘 메시지 제공 로직을 구현하고 싶다면 `FortuneProvider` 인터페이스를 구현하세요:

```java
@Component
public class CustomFortuneProvider implements FortuneProvider {
    @Override
    public String getFortune(Locale locale) {
        // 자신만의 포춘 메시지 생성 로직 구현
        return "나만의 특별한 포춘 메시지!";
    }
}
```

## 설정 옵션 상세 설명

| 속성 | 타입 | 기본값 | 설명 |
|------|------|--------|------|
| enabled | boolean | true | 포춘 쿠키 기능 활성화 여부 |
| include-header | boolean | true | HTTP 헤더에 포춘 메시지 포함 여부 |
| header-name | String | "X-Fortune-Cookie" | 포춘 메시지를 포함할 헤더 이름 |
| include-in-response | boolean | true | JSON 응답에 포춘 메시지 포함 여부 |
| response-fortune-name | String | "fortune" | JSON 응답에 포함될 필드 이름 |
| max-fortune-length | int | 0 | 포춘 메시지 최대 길이 (0 = 무제한) |
| debug | boolean | false | 디버그 모드 활성화 여부 |
| exclude-patterns | Set<String> | 빈 Set | 포춘 메시지를 제외할 URL 패턴들 |

## 라이선스
MIT License

## 기여하기
버그 리포트, 새로운 기능 제안, 풀 리퀘스트 모두 환영합니다!

## 제작자
- stark97 (https://github.com/wlsdks)

## 변경 이력
- 0.1.0
    - 최초 릴리스
    - 기본 기능 구현
    - Spring Boot 3.x 지원