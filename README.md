# spring-fortune-cookie
"Spring Fortune Cookie"은 HTTP 응답에 랜덤한 격언이나 조언을 자동으로 추가해주는 라이브러리입니다.


#### 사용법
- application.properties나 application.yml에서 이런 식으로 설정할 수 있게 됩니다.
```yaml
fortune-cookie:
  enabled: true
  include-header: true
  header-name: X-Fortune-Cookie
  include-in-response: true
  debug: true
```

