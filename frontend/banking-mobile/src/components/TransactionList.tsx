import { Text, View } from 'react-native';
import { TRANSACTION_TYPE_LABELS, TransactionResponse, TransactionType } from '@/api/types';
import { formatKRW } from '@/utils/money';

const SIGN: Record<TransactionType, '+' | '-'> = {
  DEPOSIT: '+',
  WITHDRAWAL: '-',
  TRANSFER_IN: '+',
  TRANSFER_OUT: '-',
};

const COLOR: Record<TransactionType, string> = {
  DEPOSIT: '#065F46',
  WITHDRAWAL: '#B45309',
  TRANSFER_IN: '#065F46',
  TRANSFER_OUT: '#B45309',
};

export function TransactionList({ items }: { items: TransactionResponse[] }) {
  if (items.length === 0) {
    return (
      <View style={{ padding: 16, alignItems: 'center' }}>
        <Text style={{ color: '#9CA3AF' }}>거래 내역이 없습니다.</Text>
      </View>
    );
  }

  return (
    <View style={{ gap: 1, backgroundColor: '#E5E7EB', borderRadius: 12, overflow: 'hidden' }}>
      {items.map((tx) => {
        const sign = SIGN[tx.type];
        const color = COLOR[tx.type];
        return (
          <View
            key={tx.id}
            style={{
              padding: 14,
              backgroundColor: '#FFFFFF',
              flexDirection: 'row',
              justifyContent: 'space-between',
              alignItems: 'center',
              gap: 12,
            }}
          >
            <View style={{ flex: 1, minWidth: 0 }}>
              <Text style={{ fontSize: 14, fontWeight: '600' }}>
                {TRANSACTION_TYPE_LABELS[tx.type]}
              </Text>
              {tx.counterpartyAccount && (
                <Text style={{ fontSize: 11, color: '#6B7280', marginTop: 2 }}>
                  상대: {tx.counterpartyAccount}
                </Text>
              )}
              <Text style={{ fontSize: 11, color: '#9CA3AF', marginTop: 2 }}>
                {formatInstant(tx.createdAt)}
              </Text>
            </View>
            <View style={{ alignItems: 'flex-end' }}>
              <Text style={{ fontSize: 16, fontWeight: '700', color }}>
                {sign}
                {formatKRW(tx.amount).replace('₩', '₩')}
              </Text>
              <Text
                style={{ fontSize: 11, color: '#6B7280', marginTop: 2, fontVariant: ['tabular-nums'] }}
              >
                잔액 {formatKRW(tx.balanceAfter)}
              </Text>
            </View>
          </View>
        );
      })}
    </View>
  );
}

function formatInstant(iso: string): string {
  try {
    const d = new Date(iso);
    if (Number.isNaN(d.getTime())) return iso;
    return d.toLocaleString('ko-KR', { timeZone: 'Asia/Seoul' });
  } catch {
    return iso;
  }
}
