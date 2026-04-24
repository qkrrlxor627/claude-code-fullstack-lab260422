import { Text, View } from 'react-native';
import { ACCOUNT_STATUS_LABELS, AccountStatus } from '@/api/types';

const COLORS: Record<AccountStatus, { bg: string; fg: string }> = {
  ACTIVE: { bg: '#DEF7EC', fg: '#03543F' },
  DORMANT: { bg: '#FEF3C7', fg: '#92400E' },
  CLOSED: { bg: '#FEE2E2', fg: '#991B1B' },
};

export function StatusBadge({ status }: { status: AccountStatus }) {
  const c = COLORS[status];
  return (
    <View
      style={{
        alignSelf: 'flex-start',
        backgroundColor: c.bg,
        paddingHorizontal: 10,
        paddingVertical: 4,
        borderRadius: 12,
      }}
    >
      <Text style={{ color: c.fg, fontSize: 12, fontWeight: '600' }}>
        {ACCOUNT_STATUS_LABELS[status]}
      </Text>
    </View>
  );
}
