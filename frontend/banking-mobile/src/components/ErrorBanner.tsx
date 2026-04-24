import { Text, View } from 'react-native';
import { ApiError } from '@/api/types';

const FRIENDLY: Record<string, string> = {
  ACCOUNT_NOT_FOUND: '계좌를 찾을 수 없습니다',
  ACCOUNT_ALREADY_CLOSED: '이미 해지된 계좌입니다',
  ACCOUNT_NOT_EMPTY: '잔액이 남아있어 해지할 수 없습니다',
  INVALID_AMOUNT: '금액은 0보다 커야 합니다',
  INSUFFICIENT_BALANCE: '잔액이 부족합니다',
  SELF_TRANSFER: '자기 자신에게는 이체할 수 없습니다',
  IDEMPOTENCY_CONFLICT: '멱등성 키 처리 중 충돌이 발생했습니다',
  NETWORK_ERROR: '서버에 연결할 수 없습니다',
  TIMEOUT: '서버 응답이 지연되고 있습니다',
};

export function ErrorBanner({ error }: { error: unknown }) {
  if (!error) return null;
  let title = '오류가 발생했습니다';
  let detail: string | null = null;
  let code: string | null = null;

  if (error instanceof ApiError) {
    code = error.code;
    title = FRIENDLY[error.code] ?? error.message;
    if (FRIENDLY[error.code] && error.message && error.message !== title) {
      detail = error.message;
    }
  } else if (error instanceof Error) {
    title = error.message;
  }

  return (
    <View
      style={{
        backgroundColor: '#FEF2F2',
        borderWidth: 1,
        borderColor: '#FECACA',
        borderRadius: 12,
        padding: 12,
        gap: 4,
      }}
    >
      <Text style={{ color: '#991B1B', fontWeight: '600' }}>{title}</Text>
      {detail && <Text style={{ color: '#7F1D1D', fontSize: 12 }}>{detail}</Text>}
      {code && (
        <Text style={{ color: '#B91C1C', fontSize: 10, fontFamily: 'Menlo' }}>code: {code}</Text>
      )}
    </View>
  );
}
