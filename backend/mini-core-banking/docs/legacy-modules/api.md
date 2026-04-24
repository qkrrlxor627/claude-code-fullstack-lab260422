# api module

> REST 진입점. Controller / DTO / ExceptionHandler / Swagger 설정만 둔다.

## 규칙

- **Controller 는 검증 + 위임만.** 비즈니스 로직은 `domain` 서비스에 위임.
- **DTO 는 api 모듈 내부에서만 사용.** 엔티티를 외부로 노출 금지.
- **이체 API 는 멱등성 키를 헤더 (`X-Idempotency-Key`) 로 수신** — 쿼리스트링/바디 금지.
- **모든 금액 요청/응답은 `String` 타입** (JSON 부동소수점 방지).
  - 수신: `@JsonDeserialize` 로 String → BigDecimal
  - 응답: DTO 필드 타입을 String 으로 두거나 `@JsonSerialize(using = ToStringSerializer.class)`
- **Swagger 어노테이션 필수**: `@Tag`, `@Operation`, `@Parameter` 모두 채울 것.
- **에러 응답**: 절대 Controller 에서 try-catch 후 200 리턴 금지. `GlobalExceptionHandler` 에 위임.

## 레이아웃

```
api/src/main/java/com/banking/api/
├── BankingApplication.java           ← @SpringBootApplication(scanBasePackages = "com.banking")
├── common/
│   └── GlobalExceptionHandler.java   ← @RestControllerAdvice
├── ping/                              ← 예시 엔드포인트 (Phase 1)
└── {domain}/                          ← Phase 2+ 에서 account/, transaction/, transfer/ 추가
    ├── {Domain}Controller.java
    ├── dto/
    │   ├── {Domain}Request.java
    │   └── {Domain}Response.java
    └── docs/ (선택)                    ← Swagger description 모음
```

## 테스트 정책

- Controller 단위 테스트는 `@WebMvcTest` + `MockMvc` + 서비스 Mock
- 통합 테스트는 Phase 2부터 Testcontainers + `@SpringBootTest`
- API 응답 포맷 (`ApiResponse<T>`) 고정 여부 검증 테스트 반드시 포함
