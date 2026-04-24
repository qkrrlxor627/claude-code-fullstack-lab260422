import { Text, View } from 'react-native';
import { ACCOUNT_TYPE_LABELS, AccountResponse } from '@/api/types';
import { formatKRW } from '@/utils/money';
import { StatusBadge } from './StatusBadge';

export function AccountCard({ account }: { account: AccountResponse }) {
  return (
    <View
      style={{
        padding: 20,
        borderRadius: 16,
        backgroundColor: '#FFFFFF',
        borderWidth: 1,
        borderColor: '#E5E7EB',
        gap: 12,
      }}
    >
      <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
        <Text style={{ fontSize: 12, color: '#6B7280' }}>
          {ACCOUNT_TYPE_LABELS[account.type]}
        </Text>
        <StatusBadge status={account.status} />
      </View>

      <View>
        <Text style={{ fontSize: 14, color: '#6B7280' }}>계좌번호</Text>
        <Text style={{ fontSize: 18, fontWeight: '600', marginTop: 2, fontVariant: ['tabular-nums'] }}>
          {account.accountNumber}
        </Text>
      </View>

      <View>
        <Text style={{ fontSize: 14, color: '#6B7280' }}>예금주</Text>
        <Text style={{ fontSize: 16, marginTop: 2 }}>{account.holder}</Text>
      </View>

      <View>
        <Text style={{ fontSize: 14, color: '#6B7280' }}>잔액</Text>
        <Text style={{ fontSize: 28, fontWeight: '700', marginTop: 2, fontVariant: ['tabular-nums'] }}>
          {formatKRW(account.balance)}
        </Text>
      </View>

      <View style={{ flexDirection: 'row', gap: 16 }}>
        <View style={{ flex: 1 }}>
          <Text style={{ fontSize: 12, color: '#9CA3AF' }}>개설일시</Text>
          <Text style={{ fontSize: 12, marginTop: 2 }}>{formatInstant(account.openedAt)}</Text>
        </View>
        {account.closedAt && (
          <View style={{ flex: 1 }}>
            <Text style={{ fontSize: 12, color: '#9CA3AF' }}>해지일시</Text>
            <Text style={{ fontSize: 12, marginTop: 2 }}>{formatInstant(account.closedAt)}</Text>
          </View>
        )}
      </View>
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
