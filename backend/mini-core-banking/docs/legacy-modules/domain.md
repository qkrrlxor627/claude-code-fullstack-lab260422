# domain module

> 엔티티 / 도메인 서비스 / 도메인 이벤트. 프레임워크 의존 최소화.

## 규칙

- **엔티티는 Setter 금지** — 정적 팩토리 메서드 또는 빌더 패턴만 허용
  - 예: `Account.open(...)`, `Transaction.initiate(...)`
- **도메인 로직은 엔티티/도메인 서비스에** — Controller/Repository 금지
- **도메인 이벤트는 domain 모듈 내에서 정의** (`*Event` 클래스)
- **`Account.withdraw()` 는 잔액 부족 시 `InsufficientBalanceException`**
- **`LedgerEntry` 는 불변**: 정적 팩토리로만 생성, 수정/삭제 메서드 **절대 금지**
- **금액 연산은 `BigDecimal.setScale(2, RoundingMode.HALF_UP)`** 일관 적용
- **거래 상태 전이는 `Transaction.transitionTo(newState)` 한 곳에서만** — 아무 곳에서나 상태 변경 금지

## 레이아웃

```
domain/src/main/java/com/banking/domain/
├── account/
│   ├── Account.java                   ← 엔티티 + 도메인 메서드 (deposit/withdraw)
│   ├── AccountNumber.java             ← 값 객체 (채번 + 검증)
│   ├── AccountRepository.java         ← 포트 (인터페이스)
│   └── AccountService.java            ← 도메인 서비스
├── transaction/
│   ├── Transaction.java               ← 엔티티 (상태 머신)
│   ├── TransactionStatus.java
│   ├── TransactionCompletedEvent.java ← 도메인 이벤트
│   └── TransactionService.java
├── ledger/
│   ├── LedgerEntry.java               ← 불변
│   ├── LedgerAccountCode.java         ← 계정과목 enum (1000~5000)
│   └── LedgerService.java
└── transfer/
    ├── Transfer.java
    ├── IdempotencyKey.java            ← 값 객체
    └── TransferService.java
```

## 테스트 정책

- 단위 테스트 우선 (JPA 없이)
- 엔티티 테스트: 상태 전이, 불변식, 예외 케이스 모두 커버
- 도메인 서비스 테스트: Repository Mock, 이벤트 발행 검증
