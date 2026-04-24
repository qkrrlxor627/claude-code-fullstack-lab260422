    # mini-core-banking

소규모 인터넷전문은행의 **계정계(코어뱅킹)** 시스템 토이 프로젝트.

## 목적

- 면접(신한은행/IBK) 대비용 포트폴리오 — 은행 핵심 개념(수신/여신/이체/복식부기/동시성) 실제 구현
- [Claude Code](https://docs.claude.com/claude-code) 기능 학습 (Plan Mode, 커스텀 명령, 컨텍스트 관리 등)

## 기술 스택

- **Java 17 LTS**, **Spring Boot 3.3.5**, **Gradle 8.14 (Kotlin DSL)**
- **PostgreSQL 16**, **Redis 7**
- Spring Data JPA + QueryDSL (Phase 5)
- Redisson 분산 락 (Phase 3)
- SpringDoc OpenAPI 3 (Swagger UI)
- Testcontainers (Phase 2+)

## 프로젝트 구조 (멀티모듈)

```
mini-core-banking/
├── api/       REST 컨트롤러, DTO, Swagger, 예외 핸들러
├── domain/    엔티티, 도메인 서비스, 도메인 이벤트
├── infra/     JPA 구현체, Redis 설정, 외부 연동 Mock
└── common/    ApiResponse, BankingException, 유틸
```

## 빠른 시작

```bash
# 1. 인프라 기동 (PostgreSQL + Redis)
docker compose up -d

# 2. 빌드 & 테스트
./gradlew build

# 3. 부트 실행
./gradlew :api:bootRun
```

### 엔드포인트

| URL | 설명 |
|---|---|
| http://localhost:8080/swagger-ui.html | Swagger UI |
| http://localhost:8080/actuator/health | 헬스체크 |
| http://localhost:8080/api/v1/ping | 핑 (스모크) |

## Phase 로드맵

| Phase | 내용 | 상태 |
|---|---|---|
| 1 | 프로젝트 세팅 (멀티모듈, Docker, CLAUDE.md, 부트 확인) | ✅ |
| 2 | 수신 도메인 (계좌 + 입출금 + 동시성) | ⏳ |
| 3 | 이체 + 복식부기 원장 (면접 핵심) | ⏳ |
| 4 | 커스텀 명령 + 보안 리뷰 | ⏳ |
| 5 | 관리자 조회 API (QueryDSL) | ⏳ |
| 6 | 면접 준비 (ARCHITECTURE.md, Q&A, Git 정리) | ⏳ |

## 문서

- [CLAUDE.md](./CLAUDE.md) — 프로젝트 메모리 (코딩 규칙, 도메인 규칙, 면접 포인트)
- [docs/ARCHITECTURE.md](./docs/ARCHITECTURE.md) — 아키텍처 설명 (Phase 6)
- [docs/INTERVIEW_QA.md](./docs/INTERVIEW_QA.md) — 면접 예상 Q&A (Phase 6)
