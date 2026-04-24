# frontend

프론트엔드 연습 공간. 각 실험은 하위 디렉토리(`frontend/<project-name>/`)로 격리한다.

## 예정 프로젝트

- `banking-web/` — `backend/mini-core-banking` 과 연동하는 React/Next.js UI
  - 계좌 조회, 이체 폼(멱등성 키 헤더), 거래 내역 조회
  - 금액 입력/표시는 절대 `number` 금지 → `string` + BigDecimal.js

## 공통 규칙

- 모든 금액 표시는 원단위, 천단위 콤마
- API 호출은 서버 컴포넌트/서비스에 위임 (클라이언트에서 직접 호출 최소화)
- 이체 재시도는 동일 `X-Idempotency-Key` 유지
