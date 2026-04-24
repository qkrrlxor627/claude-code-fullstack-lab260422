# 플랜 로그

프로젝트에서 Plan Mode 를 통해 승인한 플랜들의 누적 기록. 각 엔트리는 `## YYYY-MM-DD — 주제` 헤더로 구분한다. 기존 엔트리는 편집하지 말고 **아래에 append 만** 한다.

---

## 2026-04-23 — 코어뱅킹 확장 (출금 + 이체 + 원장)

- 원본 플랜 파일: `~/.claude/plans/typed-snacking-hartmanis.md`
- 실행 결과: **완료** — 백엔드 + 프론트 + DB 마이그레이션 V2/V3 + Redis + 비동기 원장 분개까지 전부 구현/검증
- 검증: `./gradlew test` 통과 · `tsc --noEmit` 통과 · 브라우저 E2E (입금/출금/이체/원장) · 차변=대변 정합성 OK

### Context

`backend/mini-core-banking/` 은 당시 계좌 개설/조회/입금/해지 4개 기능만 있었고, 프론트(`frontend/banking-mobile/`)도 거기에 맞춰져 있었다. 사용자는 "백엔드 기능 전체 알짜배기로 쓰고 싶다"고 했고, CLAUDE.md 의 Phase 2+3 목표인 **출금 (비관적 락) → 이체 (멱등성 + 데드락 방지) → 원장 (복식부기 비동기 분개)** 까지 확장해서 은행 시스템의 핵심 구간을 실제로 체험하는 것이 목표. 프론트도 같은 속도로 따라가서 브라우저에서 모든 흐름을 눌러볼 수 있도록 함.

**사용자 결정 사항**
- 범위: 출금 + 이체 + 원장 (풀 스코프)
- 프론트: 백엔드와 같이 확장
- UI 는 Expo Web + Spring Boot 위에서 확인

**코드 패턴 제약** (기존 코드 답습)
- Hexagonal: 도메인 ↔ Repository 포트 / JPA 어댑터 분리 유지
- Service 는 `@Transactional` 경계, 도메인 메서드 호출 후 `repository.save()`
- 예외는 `BankingException(code, message)` 체계로 추가
- 금액은 `BigDecimal precision=18 scale=2` + `setScale(2, HALF_UP)` 일관
- DTO 의 amount 는 regex `^[0-9]+(\.[0-9]{1,2})?$` 로 validation
- `ApiResponse<T>` 언래핑 규약 유지
- 금액은 프론트에서 `string` + `bignumber.js` (float 금지)

### 3단계 증분 전략

각 스텝은 **백엔드 → 프론트 → E2E 검증 → 커밋** 한 덩어리. 다음 스텝에 진입하기 전에 브라우저에서 해당 기능 확인.

| Step | 핵심 학습 포인트 | 대표 파일 |
|---|---|---|
| 1. 출금 | 비관적 락 `SELECT FOR UPDATE` / 잔액 부족 예외 | `Account.withdraw`, `findByNumberForUpdate` |
| 2. 이체 + 거래내역 + 멱등성 | 두 계좌 오름차순 락 (데드락 방지) / Redis 멱등키 / Transaction 엔티티 | `TransferService`, `Transaction`, `IdempotencyStore` |
| 3. 원장 (복식부기) | 비동기 이벤트 `@TransactionalEventListener(AFTER_COMMIT)` / 차변=대변 불변 | `TransactionCompletedEvent`, `LedgerEventHandler`, `LedgerEntry` |

### Step 1 — 출금

**신규 예외**: `InsufficientBalanceException` (code `INSUFFICIENT_BALANCE`)
**도메인**: `Account.withdraw(BigDecimal)` — 금액/상태/잔액 검증 후 `subtract(amount).setScale(2, HALF_UP)`
**Repository**: 포트에 `findByNumberForUpdate` 추가, JPA 측은 `@Lock(LockModeType.PESSIMISTIC_WRITE)` + JPQL
**Service**: `AccountService.withdraw` — 비관적 락으로 조회 → 도메인 호출 → 저장. 기존 `deposit()` 도 일관성 위해 락 버전으로 전환
**Controller**: `POST /api/v1/accounts/{num}/withdrawals` + `WithdrawRequest` DTO (amount string + Pattern)
**프론트**: `src/api/accounts.ts` withdraw 추가, `useWithdraw` 훅, `ErrorBanner` 에 `INSUFFICIENT_BALANCE` 매핑, 상세 화면에 입금 섹션 아래 출금 섹션

### Step 2 — 이체 + 거래 내역 + 멱등성

**Redis 활성화**: `build.gradle.kts` 의 `spring-boot-starter-data-redis` 주석 해제 (Redisson 은 스코프에서 제외 — `StringRedisTemplate` 만으로 충분), `application.yml` 에 `spring.data.redis` 추가

**V2 마이그레이션** `transactions` 테이블:
```sql
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL,
    type VARCHAR(20) NOT NULL,           -- DEPOSIT/WITHDRAWAL/TRANSFER_OUT/TRANSFER_IN
    amount NUMERIC(18,2) NOT NULL,
    counterparty_account VARCHAR(20),
    transfer_id VARCHAR(36),              -- OUT/IN 그룹 uuid
    balance_after NUMERIC(18,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_tx_account_created ON transactions(account_number, created_at DESC);
CREATE INDEX idx_tx_transfer ON transactions(transfer_id) WHERE transfer_id IS NOT NULL;
```

**도메인 / 엔티티**: `Transaction` (불변 record-like), `TransactionJpaEntity`, Repository 포트+어댑터

**Account 보강**: `AccountService.deposit/withdraw` 호출 시 `Transaction 1건` 기록 (`balance_after` 포함)

**멱등성 키 저장소**:
- `IdempotencyStore` 인터페이스 — `find`, `putIfAbsent(key, payload, ttl)`
- `RedisIdempotencyStore` — `StringRedisTemplate.opsForValue().setIfAbsent(key, json, ttl)` (24h TTL)
- 키 포맷: `idem:transfer:{clientKey}`

**이체 서비스** `TransferService.transfer(from, to, amount, idempotencyKey)`:
1. `idempotencyStore.find` 히트 → 캐시된 응답 역직렬화 반환
2. `from == to` → `SelfTransferException`
3. 락 순서 — `from.value().compareTo(to.value()) <= 0 ? [from, to] : [to, from]`
4. 두 계좌 `findByNumberForUpdate` (오름차순)
5. 도메인 `fromAccount.withdraw(amount)` + `toAccount.deposit(amount)`
6. Transaction 2건 (TRANSFER_OUT + TRANSFER_IN, 같은 `transferId` uuid)
7. 응답 구성 + `idempotencyStore.putIfAbsent`

**새 예외**: `SelfTransferException`, `IdempotencyConflictException`

**DTO + Controller**: `TransferRequest {fromAccount, toAccount, amount}`, `TransferResponse {transferId, fromAccount, toAccount, amount, fromBalanceAfter, toBalanceAfter, executedAt}`, `POST /api/v1/transfers` — `X-Idempotency-Key` 헤더 **필수** (없으면 `VALIDATION_ERROR`), `GET /api/v1/accounts/{num}/transactions?limit=20`

**GlobalExceptionHandler**: `MissingRequestHeaderException` → `VALIDATION_ERROR`

**프론트**:
- 타입: `TransactionResponse`, `TransferRequest`, `TransferResponse`
- API: `transfers.ts`, `transactions.ts`
- 훅: `useTransfer` (crypto.randomUUID 로 키 생성, 두 계좌 + 거래내역 invalidate), `useTransactions`
- 컴포넌트: `TransactionList` (부호 + 색상 + 잔액)
- 라우트: `app/accounts/[accountNumber]/transfer.tsx` (신규), 기존 detail 은 `[accountNumber]/index.tsx` 로 이동
- 상세 화면에 "이체하기" 버튼 + 하단 최근 거래 20건
- `ErrorBanner` 매핑 추가

### Step 3 — 원장 (복식부기) + 비동기 분개

**V3 마이그레이션** `ledger_entries`:
```sql
CREATE TABLE ledger_entries (
    id BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    account_code VARCHAR(20) NOT NULL,       -- 실계좌번호 또는 CASH_ASSET
    side VARCHAR(10) NOT NULL,                -- DEBIT | CREDIT
    amount NUMERIC(18,2) NOT NULL CHECK (amount > 0),
    memo VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_ledger_tx   ON ledger_entries(transaction_id);
CREATE INDEX idx_ledger_acct ON ledger_entries(account_code, created_at DESC);
```

**도메인**: `LedgerEntry` (불변, `LedgerEntry.CASH_ASSET = "CASH_ASSET"` 상수 포함), `LedgerSide` enum, Repository 포트+어댑터 (save 만, 수정/삭제 **없음**)

**이벤트**: `TransactionCompletedEvent(transactionId, accountNumber, type, amount, counterpartyAccount, transferId)` — `AccountService.deposit/withdraw`, `TransferService.transfer` 가 Transaction save 직후 `ApplicationEventPublisher.publishEvent` 로 발행

**비동기 리스너** `LedgerEventHandler`:
```java
@Async
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
@Transactional(propagation = REQUIRES_NEW)
public void onTransactionCompleted(TransactionCompletedEvent e) { ... }
```
- DEPOSIT: (차변) CASH_ASSET / (대변) 고객계좌
- WITHDRAWAL: (차변) 고객계좌 / (대변) CASH_ASSET
- TRANSFER_OUT: (차변) 출금계좌 / (대변) 수취계좌
- TRANSFER_IN: skip (OUT 에서 한 쌍 처리)
- 실패 시 로깅만 (원본 트랜잭션은 이미 커밋됨)

**활성화**: `AsyncConfig` 에 `@EnableAsync` + `ThreadPoolTaskExecutor` 빈

**조회 엔드포인트** (관리자/디버그): `GET /api/v1/ledger?accountCode=...&limit=50`, `GET /api/v1/ledger/transaction/{id}`

**프론트** (원장은 관리자 뷰로 Settings 아래 진입):
- `app/ledger.tsx` — 계좌코드 입력 → 리스트 + 차변/대변 합계 + 차액 경고
- `src/api/ledger.ts`, `src/hooks/useLedger.ts`
- `app/settings.tsx` 에 "원장 보기 (관리자)" 링크 추가
- Typed routes 를 껐음 (새 라우트 인식 문제 대응)

### 검증 결과

| 체크 | 결과 |
|---|---|
| `./gradlew test` | PASS |
| `tsc --noEmit` | PASS (EXIT=0) |
| 출금 성공 + `INSUFFICIENT_BALANCE` 에러 | OK |
| A→B 이체 후 잔액 + 거래내역 반영 | OK |
| 같은 idempotency 키 재전송 → 동일 응답 | OK (동일 `transferId`/`executedAt`) |
| A→A 이체 → `SELF_TRANSFER` | OK |
| 원장 차변 합 == 대변 합 (transaction 단위) | OK (tx#4 DEBIT=20000 CREDIT=20000, tx#5 DEBIT=5000 CREDIT=5000) |

### 리스크 & 후속 과제

- 원장 비동기 이벤트 실패 시 원본은 이미 커밋되어 롤백 불가 → 현재는 로깅만. 실제 시스템에서는 outbox 패턴 / 실패 테이블 재처리가 필요 (차기 스코프).
- Redis 미기동 시 이체 API 가 500 → fallback InMemory 구현 옵션은 현 스코프 외.
- Typed routes 는 새 라우트 추가할 때 `.expo/types` 캐시 문제로 일시 꺼둠. 필요 시 재활성화 + `expo customize tsconfig` 로 안정화 가능.

---

## 2026-04-23 — 은행 앱 필수 기능 서베이 (설계 메모)

> 실행 플랜이 아니라 **다음 스코프 후보를 고르기 위한 기능군 정리**. CI/DI 본인확인 포함 최소 세트 + 현재 프로젝트와의 갭 + 다음 MVP 후보 2 가지.

### 1. 신원 / 인증 (Identity & Auth)
- **본인확인**: CI(13자리 고유식별자) + DI(서비스별 중복가입 확인용 해시) 획득 — 보통 외부 본인확인기관(NICE/KCB) 연동
- **회원가입**: CI → 고객 레코드 생성, 최초 1회만
- **로그인 수단**: 간편비밀번호 / 생체 / 공동인증서 / 패스키
- **2차 인증**: OTP · ARS · PIN · 거래마다 재인증
- **세션 / 디바이스 관리**: 로그인 이력, 신뢰 디바이스, 강제 로그아웃
- **계정 잠금**: 비밀번호 N회 실패, 금융사기 의심 시 정지

### 2. 계좌 (Account)
- 개설 (수신: 입출금/적금/예금) · 해지
- 계좌 목록 / 단건 조회 / 잔액 조회
- 대표계좌 · 즐겨쓰는 계좌
- 휴면계좌 관리
- (금융실명법) 개설 시 실명확인 + 약관 동의 이력 저장

### 3. 입출금 / 이체 (Transactions)
- **입금**: 창구 / 현금/타행에서 들어오는 이체 수취
- **출금**: 창구 / ATM
- **이체**:
  - 당행 이체
  - 타행 이체 (금결원/오픈뱅킹)
  - 오픈뱅킹 API 연동 (다른 은행 계좌 가져와 관리)
  - 예약이체 · 자동이체(정기 스케줄)
  - 해외송금 (SWIFT)
- **수취인 확인**: 이체 직전 수취인 성함 조회 (착오송금 방지)
- **멱등성 키**: 재시도 안전성 (이미 구현됨)
- **지연이체**: 100만원 이상 30분 지연 송금 (보이스피싱 대응)

### 4. 거래 / 원장 / 조회 (History & Ledger)
- 거래 내역 조회 (기간 · 유형 · 금액 범위)
- 거래 상세 + 증빙 발급 (거래확인서 PDF)
- 당일 거래 취소
- **내부 원장** (복식부기): 감사/회계용 — 이미 구현됨

### 5. 한도 / 보안 (Limits & Risk)
- 일일/월간 이체 한도
- 1회 이체 한도
- ATM 출금 한도
- FDS (이상거래 탐지) — 평소와 다른 패턴이면 보류/차단
- 분실신고 · 사고신고 (긴급정지)
- 통장/카드 비밀번호 변경

### 6. 알림 (Notification)
- 입출금/이체 실시간 알림 (푸시 · SMS)
- 자동이체 실행/실패 알림
- 보안 경보 (새 기기 로그인)

### 7. 기타 운영 (Ops / Back-office)
- 공지사항 / 약관 / 수수료 안내
- 문의 · 상담 (챗봇 · 콜센터 연결)
- 감사 로그 (누가 언제 뭘 조회/변경했는지)

### 현재 프로젝트 vs 이 표



### 다음 MVP 후보 (택일 또는 둘 다)

- **A. 인증 라인업**: Customer(고객) 엔티티 + CI/DI + JWT 로그인 + 계좌에 `ownerCi` 연결. 이러면 "누구의 계좌인가" 관점이 생기고, `GET /accounts/me` 같은 본인 계좌 조회가 가능. 면접 포인트: CI/DI 분리 보관, 토큰 만료/갱신.
- **B. 거래 안전장치 라인업**: 이체 한도 + 수취인 확인 + 지연이체 + FDS 룰. 면접 포인트: 금융사고 대응 설계.

*결정 보류 상태*. 다음 세션에서 A 또는 B 또는 특정 기능 콕 찍어서 진행 예정.

---

## 2026-04-23 — 레이어드 → Vertical Slice 리팩터링 (실행 플랜)

- 원본 플랜 파일: `~/.claude/plans/typed-snacking-hartmanis.md`
- 실행 결과: **완료** — pure refactor, 행위/API/DB 스키마 변경 없음
- 검증: `./gradlew compileJava` PASS · `./gradlew test` PASS · bootRun 정상 기동 (6.3s) · E2E smoke (ping/개설/입금/출금/이체/원장 차변=대변) 통과

### Context

기능이 3개(account / transfer / ledger)로 늘면서 단일 `com.banking.{layer}.*` 플랫 구조의 같은 레이어 폴더에 여러 기능 파일이 섞임 (entity/ 11개, repository/ 10개, dto/ 9개, exception/ 9개). 추가 기능(CI/DI, 이체한도, FDS)이 합류하면 폴더가 50+개로 터지기 전 **기능별 vertical slice** 로 전환.

사용자 확정: **패키지 수준 vertical slice** (단일 Gradle 모듈 유지). Modulith / 멀티모듈은 범위 외.

### 트레이드오프 요약 (결정 근거)

| 관점 | Flat (현) | Vertical Slice | 승자 |
|---|---|---|---|
| 새 기능 추가 시 파일 위치 | 레이어마다 분산 | feature 폴더 하나 | VS |
| 기능 제거/이동 | 여러 폴더 탐색 | 폴더 통째로 | VS |
| 같은 레이어 패턴 참조 | 한 폴더에 다 | feature 폴더 이동 필요 | Flat |
| 스케일 (10+ 기능) | 50+ 파일 폴더 터짐 | 기능당 5~15개 | VS |
| 리팩터링 비용 (당장) | 0 | 파일 이동 + import ~100곳 | Flat |
| 런타임/빌드 성능 | 동일 | 동일 | — |

**결론**: 지금 타이밍에 돌리는 비용이 나중 비용보다 낮아서 실행.

### 실행 요약

1. 백엔드 중지 + `common/account/transaction/transfer/ledger/system` 디렉토리 생성
2. 파일 ~35개 `mv` 로 이동 (아직 커밋 전이라 git mv 대신 일반 mv — history 손실 없음)
3. `package` 선언 일괄 갱신 (경로 기반 sed)
4. import 경로 일괄 치환 — 별도 sed 스크립트로 50+개 FQCN 규칙 word boundary 매칭
5. 같은 패키지라 import 생략했던 곳 수동 추가 (BankingException → 4개 예외, *Repository 포트 → 3개 adapter, AccountNumber → Transaction, JPA entity → Type/Side 등)
6. `PersistenceConfig` 의 legacy `@EntityScan("com.banking.entity")` / `@EnableJpaRepositories("com.banking.repository")` 제거 — Spring Boot 기본 스캔이 `com.banking` 하위 전체를 커버
7. 테스트 파일도 동일 구조로 이동 (`controller/` `entity/` `repository/` → `account/` 하위)

### 최종 구조

```
com.banking/
├── BankingApplication.java
├── common/        (response, exception, config)
├── system/        (PingController)
├── account/       (Controller, Service + domain/infra/port/dto/exception)
├── transaction/   (domain/infra/port/dto/event) — 공통 부품
├── transfer/      (Controller, Service + dto/exception/idempotency)
└── ledger/        (Controller, EventHandler + domain/infra/port/dto)
```

### 의존성 방향 확인 완료 (순환 없음)

```
system  →  (의존 없음)
common  ←  (모두 참조)
transaction  →  common, account.domain(AccountNumber)
account      →  common, transaction
transfer     →  common, transaction, account
ledger       →  common, transaction
```

*주의*: `transaction` → `account.domain.AccountNumber` 한 방향 참조 있음 (원래 플랜의 "transaction 은 account 에 의존 안 함" 과는 살짝 다름). AccountNumber 는 value object 성격이라 공통 kernel 로 승격해도 되지만 현재 스코프에서는 이대로 유지. 차기 청소 대상.

### 검증 결과

| 체크 | 결과 |
|---|---|
| `./gradlew compileJava` | PASS |
| `./gradlew test` | PASS |
| bootRun 기동 (Flyway V1/V2/V3 전부 성공) | OK (6.3s) |
| GET /ping | OK |
| POST /accounts · GET /accounts/{num} | OK |
| POST /accounts/{num}/deposits | OK (A 잔액 10000) |
| POST /accounts/{num}/withdrawals | OK (A 잔액 8000) |
| POST /transfers (X-Idempotency-Key) | OK (A 5000 / B 3000) |
| GET /ledger (원장 조회 + 차변=대변) | OK (tx#7 DEPOSIT, tx#8 WITHDRAWAL, tx#9 TRANSFER 모두 정상 분개) |
| legacy 폴더(`controller`, `entity`, `repository`, `dto`, `exception`, `event`, `idempotency`, `config`) 제거 | 확인 |
| feature 폴더(`account`, `transaction`, `transfer`, `ledger`, `common`, `system`) 6개 생성 | 확인 |

### 소요 시간 & 리스크 체감

- 실제 소요: ~20분 (파일 이동 + sed 2번 + 수동 import 12곳 추가 + 2회 컴파일 fix)
- 예상과 다른 부분: "같은 패키지라 import 없이 쓰던 심볼" 의 수가 예상보다 많았음 (12곳). sed 는 FQCN 만 바꾸므로 이것들은 수동 fix 필수.
- 차기 개선: ArchUnit 도입해 "account 는 transfer 를 import 하면 테스트 실패" 를 자동 보장

---

<!-- 다음 플랜은 이 아래에 "## YYYY-MM-DD — 주제" 헤더로 append. -->
