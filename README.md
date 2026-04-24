# claude-code-fullstack-lab

Claude Code로 풀스택을 굴리면서 프롬프트, CLAUDE.md, 개발 과정을 기록하는 연습 레포.

## 구성

| 디렉토리 | 설명 |
|---|---|
| [`backend/`](./backend/) | 백엔드 실험 (Spring Boot, Kotlin DSL 멀티모듈 등) |
| [`frontend/`](./frontend/) | 프론트엔드 실험 (React, Next.js 등) |
| [`ai/`](./ai/) | AI/LLM 실험 (LangChain, RAG, MCP, Claude SDK 등) |

## 활성 프로젝트

### `backend/mini-core-banking/`
소규모 인터넷전문은행 계정계 시스템 토이 프로젝트. 면접(신한은행/IBK) 대비용.
- 수신/여신/이체/복식부기/멱등성/분산락 구현
- 자세한 내용: [backend/mini-core-banking/README.md](./backend/mini-core-banking/README.md)

## 공통 규칙

- 모든 프로젝트는 독립된 하위 디렉토리로 격리
- 각 프로젝트는 자체 `CLAUDE.md`, `README.md`, 빌드 설정을 가짐
- 루트 [`CLAUDE.md`](./CLAUDE.md) 는 랩 전체의 공통 규칙 (Git, 커밋 컨벤션, 보안 기본)
