# Spring Fortune Cookie Library

**Spring Fortune Cookie Library**는 **Spring Boot 3** 이상 환경에서 **간단한 설정**만으로 HTTP 응답(헤더 & JSON 바디)에 랜덤 포춘(운세) 메시지를 자동으로 추가해주는 라이브러리입니다.

> **목표**
> - API 응답에 재미 요소(포춘 메시지)를 부가
> - 사용자 커스터마이징과 다국어(i18n) 지원
> - Spring Boot의 Auto-Configuration에 의해, 의존성 추가 후 자동 동작

---

## ✨ 주요 기능

1. **자동 포춘 메시지 삽입**
   - HTTP 응답 헤더(`X-Fortune-Cookie`) + JSON 바디에 동시 추가
2. **플레이스홀더(Placeholder) 치환**
   - `{userName}` 등 동적 변수(헤더/세션/기타) 자동 매핑
3. **다국어(i18n) 지원**
   - `fortunes_ko.properties`, `fortunes_en.properties` 등 여러 언어 메시지
4. **커스터마이징 옵션**
   - URL 패턴 필터링(`excludePatterns`), 특정 상태 코드만 적용(`includedStatusCodes`), 에러 응답 포함(`includeOnError`) 등
   - 메시지 최대 길이(`maxFortuneLength`), 디버그 모드(`debug`)
   - 커스텀 메시지 경로(`customMessagesPath`)
5. **Auto-Configuration**
   - Spring Boot Starter처럼 설정 파일(`application.yml`)만 추가하면 동작

---

## 🚀 시작하기

### 1) 로컬 Maven 저장소에 라이브러리 설치

```bash
./gradlew clean build publishToMavenLocal
```

### 2) Gradle 의존성 추가

```gradle
repositories {
    mavenLocal()  // 로컬 Maven 저장소 우선
    mavenCentral()
}

dependencies {
    implementation 'io.github.wlsdks:spring-fortune-cookie:0.2.0-SNAPSHOT'
}
```

### 3) 기본 설정 (application.yml)

```yaml
fortune-cookie:
  enabled: true                   # 라이브러리 활성화 여부
  include-header: true            # 헤더에 포춘 메시지 추가 여부
  header-name: X-Fortune-Cookie   # 포춘 메시지를 포함할 헤더 이름
  include-in-response: true       # JSON 바디에 메시지 추가 여부
  response-fortune-name: fortune  # JSON 응답에 포함될 필드 이름
  fortunes-count: 50              # 포춘 메시지 총 개수 (기본 50)
  debug: false                    # 디버그 모드 (true 시 상세 로그)
```

이렇게 설정하면, **모든 HTTP 요청**에 랜덤 포춘 메시지가 헤더와 JSON 바디에 자동 추가됩니다.

---

## 📝 사용 예시

### 단순 컨트롤러

```java
@RestController
@RequestMapping("/api")
public class DemoController {

    @GetMapping("/hello")
    public Map<String, String> hello() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello World!");
        return response;
    }
}
```

#### 응답 예시 (JSON 바디)
```json
{
  "message": "Hello World!",
  "fortune": "오늘은 행운이 가득한 날입니다!"
}
```

#### 응답 헤더
```http
X-Fortune-Cookie: Today is a day full of luck!
```

---

## ⚙️ 설정 옵션 상세

`application.yml`에서 `fortune-cookie` 프리픽스로 다양한 설정 변경 가능:

---

| 설정 항목                 | 타입              | 기본값                        | 설명                                                                                          |
|--------------------------|-------------------|--------------------------------|-------------------------------------------------------------------------------------------------|
| `enabled`                | boolean           | `true`                         | 라이브러리 전체 활성/비활성                                                                     |
| `include-header`         | boolean           | `true`                         | 응답 헤더에 포춘 메시지 포함 여부                                                               |
| `header-name`            | String            | `"X-Fortune-Cookie"`           | 포춘 메시지를 담을 헤더 이름                                                                    |
| `include-in-response`    | boolean           | `true`                         | JSON 바디에 포춘 메시지 추가할지 여부                                                           |
| `response-fortune-name`  | String            | `"fortune"`                    | JSON 바디에 추가될 필드 이름                                                                    |
| `includedStatusCodes`    | Set<Integer>       | 빈 Set (`[]`)                 | 특정 상태 코드에만 메시지 삽입 (비어있으면 모든 상태 코드)                                      |
| `excludePatterns`        | Set<String>        | 빈 Set (`[]`)                 | 특정 URL 패턴을 포춘 메시지에서 제외                                                            |
| `includeOnError`         | boolean           | `true`                         | 에러 응답에도 메시지를 포함할지                                                                 |
| `maxFortuneLength`       | int               | `0`                            | 메시지 최대 길이 (0 = 무제한)                                                                   |
| `debug`                  | boolean           | `false`                        | 디버그 모드 (true면 메시지 생성/치환 로깅 등 상세 출력)                                         |
| `customMessagesPath`     | String            | `""` (빈 문자열)               | 사용자 정의 메시지 파일 경로                                                                    |
| `fortunesCount`          | int               | `50`                           | 메시지 총 개수 (기본 fortunes 파일에서 1~50 인덱스)                                              |
| **`placeholder-enabled`** | boolean           | `false`                        | 플레이스홀더 치환 기능 활성화 여부 (true이면 `{userName}` 등의 플레이스홀더를 실제 값으로 치환) |
| **`placeholder-mapping`** | Map<String,String>| 빈 맵 (`{}`)                  | `{플레이스홀더명}: "header:X-User-Name"` 식으로 치환 규칙을 정의 (header/session/security 등)    |

---

### 플레이스홀더(Placeholder) 기능

`{userName}`, `{today}` 등 런타임 변수를 메시지 안에서 치환하려면 다음 옵션들을 추가할 수 있습니다:

```yaml
fortune-cookie:
  placeholder-enabled: true
  placeholder-mapping:
    userName: "header:X-User-Name"
    userEmail: "header:X-User-Email"
```
- `placeholder-enabled`: 플레이스홀더 치환 기능 활성화
- `placeholder-mapping`: `{플레이스홀더명}` → “header:헤더키”, “session:세션Key” 등 매핑  
  예) `userName: "header:X-User-Name"` 이면 `{userName}`을 `X-User-Name` 헤더 값으로 치환

**메시지 파일 예시** (`fortunes_en.properties`):
```properties
fortune.1=Hello, {userName}! Today is your lucky day!
fortune.2=Welcome back, {userName}! Your email is {userEmail}.
fortune.default=Your future is uncertain.
```

**요청 헤더**:
```http
GET /api/hello
X-User-Name: Tony
X-User-Email: tony@stark.com
```

**응답(JSON 바디)**:
```json
{
   "message": "Hello World!",
   "fortune": "Welcome back, Tony! Your email is tony@stark.com."
}
```
---

## 🔧 커스터마이징

### 1. 메시지 파일 오버라이딩
사용하는 프로젝트에서 메시지를 커스터마이징하려면 동일한 경로에 properties 파일을 생성하면 됩니다:

```
src/main/resources/fortunes/fortunes_ko.properties  # 한국어 메시지
src/main/resources/fortunes/fortunes_en.properties  # 영어 메시지
```

예시:
- fortunes_ko.properties
```properties
fortune.1=우리 회사만의 특별한 메시지1
fortune.2=우리 회사만의 특별한 메시지2
fortune.3=우리 회사만의 특별한 메시지3
fortune.4=우리 회사만의 특별한 메시지4
fortune.5=우리 회사만의 특별한 메시지5
fortune.default=우리 회사의 기본 메시지입니다
fortune.special=와.. 이게 당첨된다고?? 1% 확률로 나오는 메시지입니다.
```

- fortunes_en.properties
```properties
fortune.1=Company special message 1
fortune.2=Company special message 2
fortune.3=Company special message 3
fortune.4=Company special message 4
fortune.5=Company special message 5
fortune.default=Company default message
fortune.special=Wow.. Is this a winning message?? This is a 1% probability message.
```

그리고 application.yml에서 메시지 수 설정:
```yaml
fortune-cookie:
  fortunes-count: 5  # 실제 메시지 수에 맞게 설정 (default, special은 포함안됨)
```

### 2) 커스텀 FortuneProvider

`FortuneProvider` 인터페이스를 구현하면 DB/외부 API에서 메시지를 가져올 수도 있습니다:

```java
@Component
public class DatabaseFortuneProvider implements FortuneProvider {
    
    @Override
    public String generateFortuneKey() {
        // DB 조회 후 무작위 ID 결정
        return "fortune.123";
    }

    @Override
    public String getFortune(String fortuneKey, Locale locale) {
        // DB나 외부 API에서 fortuneKey로 메시지 가져오기
        return "DB-based fortune message!";
    }
}
```
`@ConditionalOnMissingBean(FortuneProvider.class)` 덕분에, 새 Provider가 등록되면 `DefaultFortuneProvider` 대신 사용됩니다.

---

## 📄 라이선스

- [MIT License](http://www.opensource.org/licenses/mit-license.php)

---

## 🤝 기여하기

1. 이슈(버그, 기능 제안) 등록
2. Pull Request로 개선 사항 제출
3. 사용 후기나 별점(Star) 부탁드립니다

---

## 💬 문의

- GitHub: [@stark97](https://github.com/wlsdks)

---

## 📋 변경 이력

### **0.1.0-SNAPSHOT**
- 초기 릴리스: HTTP 응답(헤더 & 바디)에 자동 포춘 메시지 삽입
- 다국어(i18n) 지원 (ko/en)
- Spring Boot 3.x Auto Configuration
- `FortuneProvider` 커스텀 가능
- 주요 설정( `fortunesCount`, `debug`, `excludePatterns` 등) 제공

### **0.2.0-SNAPSHOT**
- 플레이스홀더(Placeholder) 치환 기능 추가
- 1% 확률 레어 메시지 (Gamification)
- 요일별(월/금) 메시지 로직 추가

---