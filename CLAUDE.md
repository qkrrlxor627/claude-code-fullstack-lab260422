# claude-code-fullstack-lab — 공통 규칙

이 파일은 루트 레포 전체에 적용되는 공통 규칙이다.
각 하위 프로젝트(`backend/*`, `frontend/*`, `ai/*`)는 자체 `CLAUDE.md` 에서
이 규칙을 확장/override 할 수 있다 (더 구체적인 규칙이 우선).

## 레포 구조 원칙

- **프로젝트 단위 격리**: `backend/<project>`, `frontend/<project>`, `ai/<project>` 로 분리
- **교차 참조는 최소화**: 프로젝트 간 import 금지. 필요하면 HTTP/gRPC API 경계로 연동
- **빈 디렉토리에 README.md placeholder** 를 두어 용도 명시

## Git / 커밋

- 커밋 접두사: `feat: / fix: / refactor: / test: / docs: / chore:`
- 한글 커밋 메시지 허용 (선호)
- 한 커밋에 관심사 여러 개 금지 — 쪼갤 것
- 프로젝트 단위 브랜치 권장: `feature/<project>/<topic>` (예: `feature/mini-core-banking/transfer`)
- main 은 항상 빌드 가능한 상태 유지

## 민감정보 / 보안

- `.env`, `*.key`, `credentials.json` 등 시크릿 파일은 **절대 커밋 금지**
- 모든 프로젝트는 `.gitignore` 에 환경 파일 패턴 포함
- API 키는 환경변수로만 주입
- AI 프롬프트 로그도 PII 포함 시 커밋 금지

## 개발 환경

- OS: Windows 11 + WSL/Git Bash 기준 (경로는 가능하면 forward slash 사용)
- Docker Desktop 필요 (DB, Redis 등 인프라 컨테이너 기동)
- 각 프로젝트는 자체 docker-compose.yml 보유

## Claude Code 운영

- 각 프로젝트 진입 시 해당 프로젝트 `CLAUDE.md` 우선
- 크로스 프로젝트 작업은 루트 `CLAUDE.md` 만 참조
- Plan Mode (`ultrathink`) 를 복잡한 설계 결정에 적극 사용
- `/compact` 는 **선택적 지시** 와 함께 사용 (예: `/compact 핵심 흐름만 보존`)
- `/clear` 는 관심사 전환 시 (Phase 전환 등)

## Plan Logging Rule

Plan Mode 를 통해 승인된 플랜은 **항상** `./docs/plan-log.md` 에 append 한다.

- 포맷: 파일 맨 아래(`<!-- 다음 플랜은 ... -->` 주석 위) 에 `## YYYY-MM-DD — 주제` 헤더로 새 섹션 추가
- 기존 엔트리는 **수정하지 않는다** — 항상 append 만
- 플랜 본문은 헤딩 한 단계 demote (원본 `##` → `###`)
- 실행 결과/검증 결과도 같이 기록 (완료 여부, 주요 PASS/FAIL)
- 플랜 원본은 세션 임시 파일(`~/.claude/plans/*.md`)에 남지만 이 로그가 **프로젝트의 지속 기록**

## 활성 프로젝트 (상세는 각 README 참고)

- `backend/mini-core-banking/` — 코어뱅킹 토이 (Spring Boot 3.3.5 + PostgreSQL 16 + Redis 7)
