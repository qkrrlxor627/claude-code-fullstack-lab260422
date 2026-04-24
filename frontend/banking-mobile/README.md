# banking-mobile

`backend/mini-core-banking` 백엔드와 연동하는 Expo (React Native + TypeScript) 모바일 앱.
계좌 개설 / 조회 / 입금 / 해지 4개 플로우를 iOS / Android / 웹에서 동일한 코드로 사용할 수 있다.

## 요구 사항

- Node 18 이상 (권장: 20 LTS 이상)
- 백엔드 `backend/mini-core-banking` 가 기동 중
- (선택) Android 에뮬레이터, 실기기에서는 Expo Go 앱

## 설치 & 실행

```bash
# 1. 의존성 설치
npm install

# 2. 환경변수 설정 (EXPO_PUBLIC_API_BASE_URL)
cp .env.example .env
# 필요 시 타겟 환경에 맞게 수정

# 3. 개발 서버 기동
npm start
# 이후 Expo CLI 프롬프트에서:
#   w → 웹 브라우저
#   a → Android 에뮬레이터
#   i → iOS 시뮬레이터 (Mac 한정)
#   QR 코드 → Expo Go 로 실기기
```

## 환경변수 (`EXPO_PUBLIC_API_BASE_URL`)

실행 타겟에 따라 `localhost` 의 의미가 달라진다.

| 타겟 | 값 |
|---|---|
| Expo Web (브라우저) | `http://localhost:8080/api/v1` |
| Android 에뮬레이터 | `http://10.0.2.2:8080/api/v1` |
| iOS 시뮬레이터 (Mac) | `http://localhost:8080/api/v1` |
| 실기기 Expo Go (같은 Wi-Fi) | `http://<PC-LAN-IP>:8080/api/v1` |

> **참고**: Expo 의 `EXPO_PUBLIC_*` prefix 는 빌드 타임에 번들에 포함된다. 값을 바꾼 뒤에는 `npm start` 를 재시작해야 반영된다.

## 디렉토리 구조

```
app/                    Expo Router (파일 기반 라우팅)
├── _layout.tsx         QueryClient + Stack 네비게이터
├── index.tsx           홈 (계좌 조회 / 개설 진입)
├── accounts/
│   ├── new.tsx         계좌 개설 폼
│   └── [accountNumber].tsx  상세 (입금 / 해지)
└── settings.tsx        API URL 확인 & 핑

src/
├── api/                axios 클라이언트 + ApiResponse 언래핑
├── hooks/              React Query 훅 (계좌 조회/개설/입금/해지/핑)
├── components/         AccountCard, MoneyInput, ErrorBanner, StatusBadge
└── utils/              money(BigNumber), accountNumber 정규화
```

## 금액 취급 원칙

- JS `number` 사용 **금지**. 모든 금액은 `string` + `bignumber.js`.
- 백엔드 `ApiResponse` 가 `BigDecimal` 을 plain string 으로 직렬화하므로
  그대로 state 에 넣고, 표시/연산 시 `new BigNumber(value)` 로 변환.
- 입금 폼에서 로컬 검증 후 서버 왕복. 서버 에러는 `ErrorBanner` 로 코드→한글 매핑.

## 백엔드 연동 스펙 요약

| Method | Path | 설명 |
|---|---|---|
| GET | `/api/v1/ping` | 서버 헬스 체크 |
| POST | `/api/v1/accounts` | 계좌 개설 |
| GET | `/api/v1/accounts/{accountNumber}` | 계좌 조회 |
| POST | `/api/v1/accounts/{accountNumber}/deposits` | 입금 |
| DELETE | `/api/v1/accounts/{accountNumber}` | 계좌 해지 |

응답 엔벨로프: `{ success, data, error:{code,message}, timestamp }`.
`src/api/client.ts` 가 자동으로 언래핑하고, 실패 시 `ApiError(code, message, status)` 로 throw 한다.

## 향후 확장

- 출금 API 추가 → `src/api/accounts.ts` 에 `withdraw()` + `src/hooks/useWithdraw.ts` + 상세 화면의 입금 섹션 옆에 출금 섹션 추가
- 이체 API 추가 → `app/accounts/[accountNumber]/transfer.tsx` 신규 라우트. `X-Idempotency-Key` 는 `crypto.randomUUID()` 생성해서 헤더로 전송 (루트 `frontend/README.md` 규칙)
