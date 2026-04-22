# mini-core-banking

> 소규모 인터넷전문은행의 계정계(코어뱅킹) 시스템 토이 프로젝트.
> 면접(신한은행/IBK) 대비용 + Claude Code 기능 학습용.

## 아키텍처

- **멀티모듈 Gradle (Kotlin DSL)**
  - `api` — REST 컨트롤러, DTO, Swagger, 예외 핸들러
  - `domain` — 엔티티, 도메인 서비스, 도메인 이벤트
  - `infra` — Redis 설정, 외부 연동 Mock, JPA 구현체
  - `common` — ApiResponse, BankingException, 유틸
- **계정계 패턴**: 계좌(Account) → 거래(Transaction) → 원장(LedgerEntry)
- **이벤트 기반**: `TransactionCompletedEvent` → `LedgerEventHandler` (비동기 자동 분개)
- **모든 거래는 복식부기 원칙**: 차변 합계 = 대변 합계 (위반 시 거래 롤백)

## 도메인 절대 원칙 (위반 시 PR 반려)

| 원칙 | 이유 |
|---|---|
| 모든 금액은 `BigDecimal`, `double/float` 사용 **금지** | IEEE 754 부동소수점 오차는 금융에서 치명적 |
| 계좌 잔액 직접 UPDATE 금지 — 반드시 `Transaction` 을 통해서만 변경 | 감사 추적성 + 원장 정합성 |
| 출금 시 잔액 검증은 `SELECT FOR UPDATE` (비관적 락) | 동시 출금 경쟁 시 잔액 음수 방지 |
| 이체 시 두 계좌 락은 `accountId` 오름차순으로 획득 | A→B + B→A 동시 실행 시 데드락 방지 |
| 이체는 멱등성 키 (`X-Idempotency-Key`) 로 중복 방지 | 네트워크 재시도 안전성 |
| 거래 완료 후 원장 분개는 Spring Event 비동기 처리 (`@TransactionalEventListener(phase = AFTER_COMMIT)`) | 트랜잭션 롤백 시 원장 분개도 자동 취소 |
| `LedgerEntry` 는 **불변**: 수정/삭제 불가 | 회계 감사 원칙 |

## 코딩 규칙

- 패키지: `com.banking.{모듈명}`
- 엔티티 기본 필드: `id(Long)`, `createdAt`, `updatedAt`
- 금액 컬럼: `@Column(precision = 18, scale = 2)`
- API 응답: `ApiResponse<T>` 래핑 필수
- 예외: `BankingException` 계층 (`InsufficientBalanceException`, `AccountNotFoundException` 등)
- 테스트: Given-When-Then 패턴, 한글 메서드명 허용 (`@DisplayName` 적극 활용)
- 금액 비교는 `compareTo()` — `equals()` 는 scale 까지 비교하므로 금지

## 커밋 규칙

- 형식: `feat: / fix: / refactor: / test: / docs: / chore:`
- 한글 커밋 메시지
- 기능 단위로 쪼갤 것 (한 커밋에 관심사 여러 개 섞지 말 것)
- Phase별로 `feature/phase-N-xxx` 브랜치 → main 으로 머지

## Phase 진행

- ✅ **Phase 1** — 프로젝트 세팅 (멀티모듈, Docker Compose, CLAUDE.md, 부트 확인)
- ⏳ **Phase 2** — 수신 도메인 (계좌 + 입출금)
- ⏳ **Phase 3** — 이체 + 원장 (면접 핵심)
- ⏳ **Phase 4** — 커스텀 명령 + 보안 리뷰
- ⏳ **Phase 5** — 관리자 조회 (QueryDSL)
- ⏳ **Phase 6** — 면접 준비 (ARCHITECTURE.md, Q&A, Git 정리)

## 면접 포인트 (7iTAX 연결)

| 이 프로젝트 | 7iTAX | 실제 은행 시스템 |
|---|---|---|
| `SELECT FOR UPDATE` (출금) | `SELECT FOR UPDATE` (QR 결제) | 계정계 잔액 차감 |
| `TransactionCompletedEvent` → `LedgerEntry` | `PaymentCapturedEvent` → `BookEntry` | 거래 후 자동 원장 전기 |
| Redis 멱등성 키 | Redis 분류 결과 캐시 | 전문 중복 방지 |
| Redisson 분산 락 | (미적용) | 실시간 잔액 동시성 |
| 거래 상태 머신 | 결제 authorize → capture → cancel | 전문 상태 관리 |
| `ExternalBankClient` Mock | (미적용) | 금결원/CMS 대외계 연동 |
| 복식부기 차/대변 | BookEntry 자동 생성 | 은행 총계정원장 |
| QueryDSL 동적 조회 | (미적용) | 계정계 ↔ 조회계 분리 |

## 로컬 개발

```bash
# 인프라 기동 (PostgreSQL + Redis)
docker compose up -d

# 빌드 & 테스트
./gradlew build

# 부트 실행
./gradlew :api:bootRun

# 확인
# - Swagger UI: http://localhost:8080/swagger-ui.html
# - Actuator:  http://localhost:8080/actuator/health
# - Ping:      http://localhost:8080/api/v1/ping
```

## 기술 스택 메모

- Java 17 LTS, Spring Boot 3.3.5, Gradle 8.14 (Kotlin DSL)
- PostgreSQL 16, Redis 7
- Spring Data JPA + QueryDSL (Phase 5 에서 도입)
- Redisson 분산 락 (Phase 3 에서 도입)
- SpringDoc OpenAPI 3 (Swagger UI)
- Testcontainers (Phase 2 이후 통합 테스트)
