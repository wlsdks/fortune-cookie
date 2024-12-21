# Fortune Cookie Library

**Fortune Cookie Library**는 **Spring Boot 3** 이상 환경에서 **간단한 설정**만으로 HTTP 응답(헤더 & JSON 바디)에 랜덤 포춘(운세) 메시지를 자동으로
추가해주는 라이브러리입니다.


> Spring 팀과의 연관성 안내 (Spring Team Relationship Notice):
> - 본 라이브러리는 Spring Framework/Spring Boot와 공식적으로 연관된 프로젝트가 아닙니다.
> - Spring 기반의 라이브러리를 제공하기 위해 설계되었으며, 독립적으로 개발되었습니다.
> - This library is not officially affiliated with Spring Framework/Spring Boot.
> - It is designed to provide a library based on Spring and developed independently.

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
    - `fortunes_ko.properties`, `fortunes_en.properties` 등 다양한 언어 메시지 지원
4. **커스터마이징 옵션**
    - URL 패턴 필터링(`excludePatterns`), 특정 상태 코드만 적용(`includedStatusCodes`), 에러 응답 포함(`includeOnError`)
    - 메시지 최대 길이(`maxFortuneLength`), 디버그 모드(`debug`), 커스텀 메시지 경로(`customMessagesPath`)
5. **모드 전환 기능 (0.0.3 추가)**
    - `fortune`, `joke`, `quote` 등 모드에 따라 다른 메시지 세트 사용 가능
6. **미니 게임 기능 (0.0.3 추가)**
    - 숫자 맞히기 게임을 통해 단순한 메시지 제공을 넘어 상호작용 가능
7. **Auto-Configuration**
    - Spring Boot Starter처럼 설정 파일(`application.yml`)만 추가하면 자동 동작

---

## 🚀 시작하기

### 🚨 Deprecated Notice
> 중요 공지 (Important Notice) :
  > - spring-fortune-cookie 아티팩트는 fortune-cookie로 이름이 변경되었으며, 더 이상 업데이트나 유지보수가 제공되지 않습니다. 
  > - Maven Central에는 기존의 spring-fortune-cookie가 남아있을테지만 사용자들은 모두 다음과 같이 의존성을 업데이트해 주시기 바랍니다. 
  > - The artifact spring-fortune-cookie has been renamed to fortune-cookie and is now deprecated. This means no further updates or maintenance will be provided for spring-fortune-cookie. Please update your dependencies to use the new artifact.

> 스프링 프로젝트와의 혼동 방지 설명 (Avoiding Confusion with Spring Projects):
  > - 프로젝트 이름에서 spring 접두사가 제거된 이유는 Spring Framework/Spring Boot와 공식적으로 연관된 프로젝트라는 혼동을 방지하기 위함입니다. 
  > - This change was made to avoid confusion with officially affiliated Spring Framework/Spring Boot projects.
  > - **fortune-cookie**는 Spring 기반 애플리케이션에서 사용할 수 있는 독립적인 라이브러리이며, Spring 팀과의 공식적인 연관은 없습니다. 
  > - fortune-cookie is an independent library designed for use in Spring-based applications and is not officially affiliated with the Spring team.

### 1) Maven Cental에서 라이브러리 추가
- Fortune Cookie는 Maven Central에 배포되어 있습니다. 프로젝트에서 다음 의존성을 추가하세요.
- Link : https://central.sonatype.com/artifact/io.github.wlsdks/fortune-cookie

#### Maven
``` groovy
<dependency>
    <groupId>io.github.wlsdks</groupId>
    <artifactId>fortune-cookie</artifactId>
    <version>0.3.1</version>
</dependency>
```

#### Gradle
``` groovy
dependencies {
    implementation 'io.github.wlsdks:fortune-cookie:0.3.1'
}
````

중요사항:
- **`spring-boot-starter-web`** 의존성을 먼저 추가하여 웹 관련 의존성(`spring-webmvc`, `jakarta.servlet-api`, `slf4j-api` 등)이 제공되도록 합니다.
- **`fortune-cookie`** 라이브러리는 `compileOnly`로 선언된 의존성들을 `spring-boot-starter-web`에서 제공받으므로, 추가적인 설정 없이 정상적으로
  작동합니다.

### 2) 기본 설정 (application.yml)

```yaml
fortune-cookie:
  enabled: true                   # 라이브러리 활성화 여부
  include-header: true            # 헤더에 포춘 메시지 추가 여부
  header-name: X-Fortune-Cookie   # 포춘 메시지를 포함할 헤더 이름
  include-in-response: true       # JSON 바디에 메시지 추가 여부
  response-fortune-name: fortune  # JSON 응답에 포함될 필드 이름
  fortunes-count: 50              # 포춘 메시지 총 개수 (기본 50)
  debug: false                    # 디버그 모드 (true 시 상세 로그)

  # 추가된 설정들
  mode: joke                       # fortune, joke, quote 중 하나 선택 가능
  game-enabled: true               # 미니 게임 기능 활성화
  game-range: 20                   # 숫자 추측 범위를 1~20으로 설정
  placeholder-enabled: true        # 플레이스홀더 사용 여부
  placeholder-mapping:
    userName: "header:X-User-Name"
    userEmail: "header:X-User-Email"
```

- 이렇게 설정하면, **모든 HTTP 요청**에 랜덤 포춘 메시지가 헤더와 JSON 바디에 자동 추가됩니다.


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

| 설정 항목                         | 타입                 | 기본값                  | 설명                                                                |
|-------------------------------|--------------------|----------------------|-------------------------------------------------------------------|
| `enabled`                     | boolean            | `true`               | 라이브러리 전체 활성/비활성                                                   |
| `include-header`              | boolean            | `true`               | 응답 헤더에 포춘 메시지 포함 여부                                               |
| `header-name`                 | String             | `"X-Fortune-Cookie"` | 포춘 메시지를 담을 헤더 이름                                                  |
| `include-in-response`         | boolean            | `true`               | JSON 바디에 포춘 메시지 추가 여부                                             |
| `response-fortune-name`       | String             | `"fortune"`          | JSON 바디에 추가될 필드 이름                                                |
| `includedStatusCodes`         | Set<Integer>       | 빈 Set (`[]`)         | 특정 상태 코드에만 메시지 삽입 (비어있으면 모든 상태 코드)                                |
| `excludePatterns`             | Set<String>        | 빈 Set (`[]`)         | 특정 URL 패턴을 포춘 메시지에서 제외                                            |
| `includeOnError`              | boolean            | `true`               | 에러 응답에도 메시지를 포함할지                                                 |
| `maxFortuneLength`            | int                | `0`                  | 메시지 최대 길이 (0 = 무제한)                                               |
| `debug`                       | boolean            | `false`              | 디버그 모드 (true면 메시지 생성/치환 로깅 등 상세 출력)                               |
| `customMessagesPath`          | String             | `""` (빈 문자열)         | 사용자 정의 메시지 파일 경로                                                  |
| `fortunesCount`               | int                | `50`                 | 메시지 총 개수 (기본 fortunes 파일에서 1~50 인덱스)                              |
| **`placeholder-enabled`**     | boolean            | `false`              | 플레이스홀더 치환 기능 활성화 (true 시 `{userName}` 등 치환)                       |
| **`placeholder-mapping`**     | Map<String,String> | 빈 맵 (`{}`)           | `{플레이스홀더명}: "header:X-User-Name"` 식으로 치환 규칙 정의 (header/session 등) |
| **`mode`** (0.0.3 추가)         | String             | `"fortune"`          | 메시지 모드 설정: `fortune`, `joke`, `quote` 중 하나 선택 가능                  |
| **`game-enabled`** (0.0.3 추가) | boolean            | `false`              | 미니 게임 기능 활성화 (true 시 숫자 맞히기 게임 실행)                                |
| **`game-range`** (0.0.3 추가)   | int                | `10`                 | 미니 게임 숫자 범위 (1~game-range 사이의 숫자 추측)                              |

---

## 🔥 0.0.3 버전 추가 기능

### 1) 모드 전환 기능

`mode` 프로퍼티를 통해 메시지 모드를 변경할 수 있습니다.

- `fortune` (기본): 기존 포춘 메시지
- `joke`: 농담 메시지(`fortune.joke.*`) 사용
- `quote`: 명언 메시지(`fortune.quote.*`) 사용

**application.yml 예시:**

```yaml
fortune-cookie:
  mode: joke   # joke 모드 설정
```

메시지 파일 예시(`fortunes_en.properties`):

```properties
fortune.joke.1=Why don’t programmers like nature? Because it has too many bugs.
fortune.joke.2=I told my computer I needed a break, and it said: 'No problem, I’ll go on a byte!'
fortune.joke.default=No jokes for you today.
```

이렇게 설정하면 호출할 때마다 농담 메시지가 랜덤하게 나타납니다.

### 2) 미니 게임 기능

`game-enabled`를 true로 설정하면, 간단한 숫자 맞히기 게임을 즐길 수 있습니다.  
`game-range`로 추측 범위를 지정할 수 있으며, 기본값은 10입니다.

사용자가 요청 시 `X-Guess` 헤더를 보내 숫자를 추측하면, 맞추면 축하 메시지를, 틀리면 재도전 메시지를 응답 바디에 추가합니다.  
`X-Guess` 헤더 없이 요청하면 안내 메시지가 표시됩니다.

**application.yml 예시:**

```yaml
fortune-cookie:
  game-enabled: true
  game-range: 20
```

**요청 예시:**

```http
GET /api/hello
X-Guess: 7
```

**응답 예시 (틀린 경우):**

```json
{
  "message": "Hello World!",
  "fortune": "오늘은 행운이 가득한 날입니다! Wrong guess! Try again!"
}
```

맞추면:

```json
{
  "message": "Hello World!",
  "fortune": "오늘은 행운이 가득한 날입니다! You guessed correctly! The secret number was 7."
}
```

---

## 플레이스홀더(Placeholder) 기능

`{userName}`, `{today}` 등 런타임 변수를 메시지 안에서 치환하려면 다음 옵션들을 추가할 수 있습니다:

```yaml
fortune-cookie:
  placeholder-enabled: true
  placeholder-mapping:
    userName: "header:X-User-Name"
    userEmail: "header:X-User-Email"
```

- `placeholder-enabled`: 플레이스홀더 치환 기능 활성화
- `placeholder-mapping`: `{플레이스홀더명}` → "header:헤더키" 등의 규칙 설정

메시지 파일 예시(`fortunes_en.properties`):

```properties
fortune.1=Hello, {userName}! Today is your lucky day!
fortune.2=Welcome back, {userName}! Your email is {userEmail}.
fortune.default=Your future is uncertain.
```

요청:

```http
GET /api/hello
X-User-Name: Tony
X-User-Email: tony@stark.com
```

응답(JSON 바디):

```json
{
  "message": "Hello World!",
  "fortune": "Welcome back, Tony! Your email is tony@stark.com."
}
```

---

## 🔧 커스터마이징

### 1. 메시지 파일 오버라이딩

사용하는 프로젝트에서 메시지 파일을 커스터마이징하면, 라이브러리보다 우선하여 적용됩니다:

```
src/main/resources/fortunes/fortunes_ko.properties
src/main/resources/fortunes/fortunes_en.properties
```

예시:

```properties
fortune.1=우리 회사만의 특별한 메시지1
fortune.2=우리 회사만의 특별한 메시지2
fortune.3=우리 회사만의 특별한 메시지3
fortune.4=우리 회사만의 특별한 메시지4
fortune.5=우리 회사만의 특별한 메시지5
fortune.default=우리 회사의 기본 메시지입니다
fortune.special=와.. 이게 당첨된다고?? 1% 확률로 나오는 메시지입니다.
```

`application.yml`에서 메시지 수 설정:

```yaml
fortune-cookie:
  fortunes-count: 5
```

### 2) 커스텀 FortuneProvider

`FortuneProvider` 인터페이스를 구현하면 DB나 외부 API에서 메시지를 가져올 수도 있습니다:

```java

@Component
public class DatabaseFortuneProvider implements FortuneProvider {

    @Override
    public String generateFortuneKey() {
        // DB 조회 후 무작위 키 결정
        return "fortune.123";
    }

    @Override
    public String getFortune(String fortuneKey, Locale locale) {
        return "DB-based fortune message!";
    }
}
```

`@ConditionalOnMissingBean(FortuneProvider.class)` 덕분에 새 Provider가 등록되면 `DefaultFortuneProvider` 대신 사용됩니다.

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

- GitHub: [@stark](https://github.com/wlsdks)

---

## 📋 변경 이력

### **0.1.0-SNAPSHOT**

- 초기 릴리스: HTTP 응답(헤더 & 바디)에 자동 포춘 메시지 삽입
- 다국어(i18n) 지원 (ko/en)
- Spring Boot 3.x Auto Configuration
- `FortuneProvider` 커스텀 가능
- 주요 설정(`fortunesCount`, `debug`, `excludePatterns` 등) 제공

### **0.2.0-SNAPSHOT**

- 플레이스홀더(Placeholder) 치환 기능 추가
- 1% 확률 레어 메시지 (Gamification)
- 요일별(월/금) 메시지 로직 추가

### **0.3.0-SNAPSHOT (정식 배포 진행)**

- **모드 전환 기능 추가**: `fortune`, `joke`, `quote` 등 모드별 메시지 사용
- **미니 게임 기능 추가**: 숫자 맞히기 게임 활성화(`game-enabled`), 범위(`game-range`) 지정 가능
- 플레이스홀더, 레어 메시지, 요일별 메시지와 함께 더욱 풍부한 사용자 경험 제공
- 스프링 프로젝트와의 혼동성을 없애기 위해 프로젝트명에서 spring을 제거 (이미 maven central에 배포된 기존 artifact는 삭제가 힘들어서 유지하고 여기에 설명을 추가)

### 0.3.1
- 프로젝트에서 jar가 빌드될때 spring-fortune-cookie로 빌드되는 문제 수정 (맨 앞의 spring은 제거했기에 없어져야함)
- 수정작업을 진행해서 이제부터는 fortune-cookie로 빌드됩니다.

---