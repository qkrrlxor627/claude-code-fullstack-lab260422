import { useMemo, useState } from 'react';
import { Pressable, ScrollView, Text, TextInput, View } from 'react-native';
import BigNumber from 'bignumber.js';
import { ErrorBanner } from '@/components/ErrorBanner';
import { useLedger } from '@/hooks/useLedger';
import { formatKRW } from '@/utils/money';

export default function LedgerScreen() {
  const [input, setInput] = useState('');
  const [accountCode, setAccountCode] = useState<string | undefined>(undefined);
  const ledger = useLedger(accountCode, 100);

  const summary = useMemo(() => {
    if (!ledger.data) return null;
    const debit = ledger.data
      .filter((e) => e.side === 'DEBIT')
      .reduce((acc, e) => acc.plus(e.amount), new BigNumber(0));
    const credit = ledger.data
      .filter((e) => e.side === 'CREDIT')
      .reduce((acc, e) => acc.plus(e.amount), new BigNumber(0));
    const diff = debit.minus(credit);
    return { debit: debit.toFixed(2), credit: credit.toFixed(2), diff: diff.toFixed(2) };
  }, [ledger.data]);

  return (
    <ScrollView contentContainerStyle={{ padding: 20, gap: 16 }}>
      <View style={{ gap: 6 }}>
        <Text style={{ fontSize: 13, color: '#6B7280' }}>
          계좌 코드 (실계좌번호 또는 {`"`}CASH_ASSET{`"`})
        </Text>
        <TextInput
          value={input}
          onChangeText={setInput}
          placeholder="110-01-1000000-X 또는 CASH_ASSET"
          autoCapitalize="characters"
          autoCorrect={false}
          style={{
            borderWidth: 1,
            borderColor: '#D1D5DB',
            borderRadius: 8,
            padding: 12,
            fontSize: 14,
            backgroundColor: '#FFFFFF',
            fontVariant: ['tabular-nums'],
          }}
        />
        <Pressable
          onPress={() => setAccountCode(input.trim() || undefined)}
          disabled={input.trim().length === 0}
          style={({ pressed }) => ({
            backgroundColor: input.trim().length === 0 ? '#9CA3AF' : pressed ? '#374151' : '#4B5563',
            borderRadius: 8,
            paddingVertical: 12,
            alignItems: 'center',
          })}
        >
          <Text style={{ color: '#FFFFFF', fontWeight: '700' }}>조회</Text>
        </Pressable>
      </View>

      {ledger.error && <ErrorBanner error={ledger.error} />}

      {summary && (
        <View style={{ backgroundColor: '#FFFFFF', padding: 14, borderRadius: 12, gap: 6 }}>
          <Text style={{ fontSize: 12, color: '#6B7280' }}>합계 — 복식부기 검증</Text>
          <Row label="차변 (DEBIT)" value={formatKRW(summary.debit)} />
          <Row label="대변 (CREDIT)" value={formatKRW(summary.credit)} />
          <View style={{ borderTopWidth: 1, borderColor: '#E5E7EB', marginTop: 4, paddingTop: 4 }}>
            <Row
              label="차액"
              value={formatKRW(summary.diff)}
              color={summary.diff === '0.00' ? '#065F46' : '#991B1B'}
            />
          </View>
          {summary.diff !== '0.00' && (
            <Text style={{ fontSize: 11, color: '#991B1B' }}>
              단일 계정 관점에서는 차액이 있을 수 있습니다. transaction 단위 정합성은 /ledger/transaction/&#123;id&#125; 로 확인하세요.
            </Text>
          )}
        </View>
      )}

      {ledger.data && ledger.data.length === 0 && (
        <Text style={{ color: '#9CA3AF', textAlign: 'center', padding: 16 }}>
          원장 기록이 없습니다.
        </Text>
      )}

      {ledger.data && ledger.data.length > 0 && (
        <View
          style={{ gap: 1, backgroundColor: '#E5E7EB', borderRadius: 12, overflow: 'hidden' }}
        >
          {ledger.data.map((e) => (
            <View
              key={e.id}
              style={{
                padding: 12,
                backgroundColor: '#FFFFFF',
                flexDirection: 'row',
                justifyContent: 'space-between',
                alignItems: 'center',
                gap: 8,
              }}
            >
              <View style={{ flex: 1 }}>
                <Text
                  style={{
                    fontSize: 12,
                    fontWeight: '700',
                    color: e.side === 'DEBIT' ? '#1E3A8A' : '#14532D',
                  }}
                >
                  {e.side === 'DEBIT' ? '차변' : '대변'}
                </Text>
                <Text style={{ fontSize: 11, color: '#6B7280', marginTop: 2 }}>
                  tx #{e.transactionId} · {e.memo ?? '-'}
                </Text>
                <Text style={{ fontSize: 10, color: '#9CA3AF', marginTop: 2 }}>
                  {new Date(e.createdAt).toLocaleString('ko-KR', { timeZone: 'Asia/Seoul' })}
                </Text>
              </View>
              <Text
                style={{
                  fontSize: 14,
                  fontWeight: '700',
                  color: e.side === 'DEBIT' ? '#1E3A8A' : '#14532D',
                  fontVariant: ['tabular-nums'],
                }}
              >
                {formatKRW(e.amount)}
              </Text>
            </View>
          ))}
        </View>
      )}
    </ScrollView>
  );
}

function Row({ label, value, color }: { label: string; value: string; color?: string }) {
  return (
    <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
      <Text style={{ fontSize: 13, color: '#6B7280' }}>{label}</Text>
      <Text style={{ fontSize: 15, fontWeight: '700', color: color ?? '#111827', fontVariant: ['tabular-nums'] }}>
        {value}
      </Text>
    </View>
  );
}
