import { useLocalSearchParams, useRouter } from 'expo-router';
import { useMemo, useState } from 'react';
import { Pressable, ScrollView, Text, TextInput, View } from 'react-native';
import { ErrorBanner } from '@/components/ErrorBanner';
import { MoneyInput } from '@/components/MoneyInput';
import { useAccount } from '@/hooks/useAccount';
import { newIdempotencyKey, useTransfer } from '@/hooks/useTransfer';
import { normalizeAccountNumberInput } from '@/utils/accountNumber';
import { formatKRW, validateAmount } from '@/utils/money';

export default function TransferScreen() {
  const params = useLocalSearchParams<{ accountNumber: string }>();
  const fromAccount = params.accountNumber;
  const router = useRouter();

  const account = useAccount(fromAccount);
  const transferMut = useTransfer();

  const [toAccount, setToAccount] = useState('');
  const [amount, setAmount] = useState('');
  // Idempotency 키는 폼 생명주기당 하나. 사용자가 같은 폼에서 "이체" 를 두 번 누르면
  // 같은 키로 전송돼서 서버가 한 번만 실행.
  const idempotencyKey = useMemo(() => newIdempotencyKey(), []);

  const amountValidation = amount.trim() === '' ? null : validateAmount(amount);
  const toNormalized = normalizeAccountNumberInput(toAccount);
  const isToValid = toNormalized.length > 0;
  const canSubmit =
    isToValid && !!amountValidation && amountValidation.ok && !transferMut.isPending;

  const handleTransfer = () => {
    if (!canSubmit || !fromAccount) return;
    transferMut.mutate(
      {
        body: {
          fromAccount,
          toAccount: toNormalized,
          amount: amountValidation!.value,
        },
        idempotencyKey,
      },
      {
        onSuccess: () => {
          router.replace(`/accounts/${encodeURIComponent(fromAccount)}`);
        },
      },
    );
  };

  if (!fromAccount) {
    return (
      <View style={{ padding: 20 }}>
        <Text>계좌번호가 없습니다.</Text>
      </View>
    );
  }

  return (
    <ScrollView contentContainerStyle={{ padding: 20, gap: 20 }}>
      <View
        style={{
          backgroundColor: '#F3F4F6',
          padding: 14,
          borderRadius: 12,
        }}
      >
        <Text style={{ fontSize: 12, color: '#6B7280' }}>출금 계좌</Text>
        <Text style={{ fontSize: 16, marginTop: 4, fontVariant: ['tabular-nums'] }}>
          {fromAccount}
        </Text>
        {account.data && (
          <Text style={{ fontSize: 12, color: '#6B7280', marginTop: 4 }}>
            현재 잔액 {formatKRW(account.data.balance)}
          </Text>
        )}
      </View>

      {transferMut.error && <ErrorBanner error={transferMut.error} />}

      <View style={{ gap: 6 }}>
        <Text style={{ fontSize: 14, color: '#6B7280' }}>받는 계좌번호</Text>
        <TextInput
          value={toAccount}
          onChangeText={setToAccount}
          placeholder="110-01-1000001-2"
          autoCapitalize="characters"
          autoCorrect={false}
          style={{
            borderWidth: 1,
            borderColor: '#D1D5DB',
            borderRadius: 8,
            padding: 12,
            fontSize: 16,
            backgroundColor: '#FFFFFF',
            fontVariant: ['tabular-nums'],
          }}
        />
      </View>

      <View style={{ gap: 6 }}>
        <Text style={{ fontSize: 14, color: '#6B7280' }}>이체 금액</Text>
        <MoneyInput
          value={amount}
          onChangeText={setAmount}
          disabled={transferMut.isPending}
        />
      </View>

      <Pressable
        onPress={handleTransfer}
        disabled={!canSubmit}
        style={({ pressed }) => ({
          backgroundColor: !canSubmit
            ? '#9CA3AF'
            : pressed
            ? '#1D4ED8'
            : '#2563EB',
          borderRadius: 8,
          paddingVertical: 16,
          alignItems: 'center',
        })}
      >
        <Text style={{ color: '#FFFFFF', fontWeight: '700', fontSize: 16 }}>
          {transferMut.isPending ? '이체 중…' : '이체하기'}
        </Text>
      </Pressable>

      <Text style={{ fontSize: 11, color: '#9CA3AF', textAlign: 'center' }}>
        멱등성 키: {idempotencyKey.slice(0, 8)}… (재전송 시 중복 실행 방지)
      </Text>
    </ScrollView>
  );
}
