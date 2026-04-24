# ai

AI 연동 연습 공간. LangChain, RAG, MCP, Claude SDK 등.

## 예정 프로젝트

- `banking-assistant/` — `backend/mini-core-banking` API 를 호출하는 은행 챗봇 / 거래 분류
  - 자연어 이체 요청 → API 파라미터 추출
  - 거래 내역 요약 (RAG)
  - 이상 거래 탐지 (시그널 + LLM 판단)

## 공통 규칙

- 모델 출력을 그대로 SQL/API 호출에 넣지 말 것 (프롬프트 인젝션 위험)
- 금액/계좌번호는 반드시 구조화된 schema (Pydantic / Zod) 로 파싱
- 키 관리: `.env` + `gitignore` 엄수, 레포에 커밋 금지
