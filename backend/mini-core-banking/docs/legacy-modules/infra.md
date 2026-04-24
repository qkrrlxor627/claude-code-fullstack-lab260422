# infra module

> JPA 구현체, Redis 설정, 외부 연동 Mock. `domain` 의 포트를 구현.

## 규칙

- **domain 의 Repository 인터페이스를 구현**하는 형태 — 헥사고날 지향
  - 예: `domain.account.AccountRepository` (interface) ← `infra.account.AccountRepositoryJpa` (impl)
- **Redis 키 네이밍 규칙**: `banking:{도메인}:{식별자}`
  - 예: `banking:account:lock:{accountId}`, `banking:idempotency:{key}`
- **Redisson 분산 락 타임아웃**: `waitTime = 5초`, `leaseTime = 10초` 를 상수로 중앙 관리
- **타행이체 Mock 은 `ExternalBankClient` 인터페이스로 추상화** — 구현체 `ExternalBankClientMock` 은 `@Profile("local")` 또는 `@ConditionalOnMissingBean`
- **JPA Entity ↔ Domain Entity 는 분리 가능** (현재는 통합, 필요 시 매퍼 도입)

## 레이아웃 (Phase 2 이후)

```
infra/src/main/java/com/banking/infra/
├── persistence/
│   ├── account/
│   │   └── AccountJpaRepository.java  ← Spring Data JPA
│   ├── transaction/
│   └── ledger/
├── redis/
│   ├── RedisConfig.java
│   ├── RedissonConfig.java
│   └── IdempotencyStore.java
└── external/
    ├── ExternalBankClient.java        ← port (interface)
    ├── ExternalBankClientMock.java    ← mock 구현 (지연 + 실패율)
    └── KftcMessage.java               ← 금결원 전문 시뮬레이션 DTO
```

## 테스트 정책

- Redis 통합 테스트: Testcontainers `RedisContainer`
- JPA 통합 테스트: `@DataJpaTest` + Testcontainers `PostgreSQLContainer`
- 외부 연동 Mock 은 실패율/지연을 테스트에서 제어 가능해야 함 (`Clock`, `Random` 주입)
