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
# application.yml
fortune-cookie:
  enabled: true                    # 기능 활성화 여부
  include-header: true            # HTTP 헤더에 포춘 메시지 포함
  header-name: X-Fortune-Cookie   # 포춘 메시지를 포함할 헤더 이름
  include-in-response: true       # JSON 응답에 포춘 메시지 포함
  response-fortune-name: fortune  # JSON 응답에 포함될 필드 이름
  fortunes-count: 5               # 포춘 메시지 개수 (실제 메시지 수에 맞게 설정)
  debug: false                    # 디버그 모드 활성화 여부
```

## 📝 사용 예시

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

헤더에도 포춘 메시지가 포함됩니다 (영어 메시지):
```http
X-Fortune-Cookie: A positive mindset will bring positive results.
```

## ⚙️ 설정 옵션

| 설정 | 타입 | 기본값 | 설명 |
|------|------|--------|------|
| enabled | boolean | true | 기능 활성화 여부 |
| include-header | boolean | true | 헤더 포함 여부 |
| header-name | String | "X-Fortune-Cookie" | 헤더 이름 |
| include-in-response | boolean | true | JSON 응답 포함 여부 |
| response-fortune-name | String | "fortune" | JSON 필드 이름 |
| fortunes-count | int | 5 | 포춘 메시지 개수 |
| debug | boolean | false | 디버그 모드 활성화 여부 |


## 🔧 커스터마이징

### 1. 메시지 파일 오버라이딩
사용하는 프로젝트에서 메시지를 커스터마이징하려면 동일한 경로에 properties 파일을 생성하면 됩니다:

```
src/main/resources/fortunes/fortunes_ko.properties  # 한국어 메시지
src/main/resources/fortunes/fortunes_en.properties  # 영어 메시지
```

예시:
```properties
# fortunes_ko.properties
fortune.1=우리 회사만의 특별한 메시지1
fortune.2=우리 회사만의 특별한 메시지2
fortune.3=우리 회사만의 특별한 메시지3
fortune.4=우리 회사만의 특별한 메시지4
fortune.5=우리 회사만의 특별한 메시지5
fortune.default=우리 회사의 기본 메시지입니다

# fortunes_en.properties
fortune.1=Company special message 1
fortune.2=Company special message 2
fortune.3=Company special message 3
fortune.4=Company special message 4
fortune.5=Company special message 5
fortune.default=Company default message
```

그리고 application.yml에서 메시지 수 설정:
```yaml
fortune-cookie:
  fortunes-count: 5  # 실제 메시지 수에 맞게 설정
```

### 2. FortuneProvider 구현
또는 `FortuneProvider` 인터페이스를 구현하여 완전히 새로운 메시지 제공 로직을 만들 수 있습니다:

```java
@Component
public class CustomFortuneProvider implements FortuneProvider {
    @Override
    public String getFortune(Locale locale) {
        // 커스텀 로직 (예: DB에서 메시지 가져오기)
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
- 다국어 지원 (한국어/영어)
- Spring Boot 3.x 지원
- 메시지 커스터마이징 기능
- Fortune count 설정 기능