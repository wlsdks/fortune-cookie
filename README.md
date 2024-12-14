# Spring Fortune Cookie Library

간단한 설정으로 HTTP 응답에 포춘(운세) 메시지를 자동으로 추가해주는 Spring 라이브러리입니다.

## ✨ 주요 기능
- HTTP 응답에 자동으로 포춘 메시지 추가
- JSON 응답 본문 또는 HTTP 헤더를 통한 메시지 전달
- 다국어(i18n) 지원
- Spring Boot Auto Configuration 지원

## 🚀 시작하기

### 1. 로컬 Maven 저장소에 라이브러리 설치
```bash
./gradlew clean build publishToMavenLocal
```

### 2. 의존성 추가
```gradle
repositories {
    mavenLocal()  // 로컬 Maven 저장소 추가
    mavenCentral()
}

dependencies {
    implementation 'io.github.wlsdks:spring-fortune-cookie:0.1.0-SNAPSHOT'
}
```

### 3. 설정 추가
```yaml
# application.yml
fortune-cookie:
  enabled: true
  include-header: true
  header-name: X-Fortune-Cookie
  include-in-response: true
  response-fortune-name: fortune
```

### 4. 포춘 메시지 설정
`src/main/resources/fortunes/fortunes_ko.properties`:
```properties
fortune.1=오늘은 행운이 가득한 날입니다!
fortune.2=작은 시도가 큰 변화를 만들어낼 것입니다.
fortune.3=긍정적인 마음가짐이 좋은 결과를 가져올 것입니다.
fortune.4=힘들었던 일이 좋은 결과로 돌아올 것입니다.
fortune.5=특별한 인연을 만나게 될 것입니다.
fortune.default=오늘도 좋은 하루 되세요!
```

> ⚠️ 주의: DefaultFortuneProvider의 FORTUNE_COUNT 값을 실제 메시지 개수와 일치하도록 설정해야 합니다.

## 📝 사용 예시

### 스프링부트 설정코드 추가
- 컴포넌트 스캔 코드를 추가해주셔야 합니다.
```java
// 컴포넌트 스캔 코드만 추가하면 됩니다.
@ComponentScan(basePackages = {"com.example", "io.github.wlsdks.fortunecookie"})
@SpringBootApplication
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }

}
```

### 기본 사용
컨트롤러에서 일반적인 JSON 응답을 반환하면 자동으로 포춘 메시지가 추가됩니다:

```java
@RequestMapping("/api")
@RestController
public class TestController {

    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
      Map<String, String> response = new HashMap<>();
      response.put("message", "Hello World!!!");
      return ResponseEntity.ok()
              .contentType(MediaType.APPLICATION_JSON)
              .body(response);
    }
    
}
```

### 응답 예시
```json
{
    "message": "Hello World!!!",
    "fortune": "긍정적인 마음가짐이 좋은 결과를 가져올 것입니다."
}
```

헤더에도 포춘 메시지가 포함됩니다:
```http
X-Fortune-Cookie: 긍정적인 마음가짐이 좋은 결과를 가져올 것입니다.
```

## ⚙️ 설정 옵션

| 설정 | 타입 | 기본값 | 설명 |
|------|------|--------|------|
| enabled | boolean | true | 기능 활성화 여부 |
| include-header | boolean | true | 헤더 포함 여부 |
| header-name | String | "X-Fortune-Cookie" | 헤더 이름 |
| include-in-response | boolean | true | JSON 응답 포함 여부 |
| response-fortune-name | String | "fortune" | JSON 필드 이름 |

## 🔧 커스터마이징

### 메시지 추가/수정
1. `src/main/resources/fortunes/` 디렉토리에 메시지 파일 추가
2. `DefaultFortuneProvider.java`의 `FORTUNE_COUNT` 값을 메시지 개수에 맞게 수정

### 커스텀 구현
`FortuneProvider` 인터페이스를 구현하여 자신만의 메시지 제공 로직을 구현할 수 있습니다:

```java
@Component
public class CustomFortuneProvider implements FortuneProvider {
    @Override
    public String getFortune(Locale locale) {
        // 커스텀 로직
        return "나만의 포춘 메시지!";
    }
}
```

## 📄 라이선스
MIT License

## 🤝 기여하기
- 이슈 등록
- 풀 리퀘스트
- 새로운 기능 제안

## 💬 문의
- GitHub: [@stark97](https://github.com/wlsdks)

## 📋 변경 이력
### 0.1.0-SNAPSHOT
- 최초 릴리스
- HTTP 응답 포춘 메시지 자동 추가
- 다국어 지원
- Spring Boot 3.x 지원