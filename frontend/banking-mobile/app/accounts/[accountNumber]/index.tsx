import { useLocalSearchParams, useRouter } from 'expo-router';
import { useState } from 'react';
import { ActivityIndicator, Alert, Platform, Pressable, ScrollView, Text, View } from 'react-native';
import { AccountCard } from '@/components/AccountCard';
import { ErrorBanner } from '@/components/ErrorBanner';
import { MoneyInput } from '@/components/MoneyInput';
import { TransactionList } from '@/components/TransactionList';
import { useAccount } from '@/hooks/useAccount';
import { useCloseAccount } from '@/hooks/useCloseAccount';
import { useDeposit } from '@/hooks/useDeposit';
import { useTransactions } from '@/hooks/useTransactions';
import { useWithdraw } from '@/hooks/useWithdraw';
import { validateAmount } from '@/utils/money';

export default function AccountDetailScreen() {
  const params = useLocalSearchParams<{ accountNumber: string }>();
  const accountNumber = params.accountNumber;
  const router = useRouter();

  const account = useAccount(accountNumber);
  const transactions = useTransactions(accountNumber, 20);
  const depositMut = useDeposit();
  const withdrawMut = useWithdraw();
  const closeMut = useCloseAccount();

  const [depositAmount, setDepositAmount] = useState('');
  const [withdrawAmount, setWithdrawAmount] = useState('');
  const depositValidation = depositAmount.trim() === '' ? null : validateAmount(depositAmount);
  const withdrawValidation = withdrawAmount.trim() === '' ? null : validateAmount(withdrawAmount);

  if (!accountNumber) {
    return (
      <View style={{ padding: 20 }}>
        <Text>계좌번호가 없습니다.</Text>
      </View>
    );
  }

  if (account.isLoading) {
    return (
      <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
        <ActivityIndicator size="large" />
        <Text style={{ marginTop: 12, color: '#6B7280' }}>조회 중…</Text>
      </View>
    );
  }

  if (account.error || !account.data) {
    return (
      <ScrollView contentContainerStyle={{ padding: 20, gap: 16 }}>
        <ErrorBanner error={account.error ?? new Error('계좌 정보를 불러오지 못했습니다')} />
        <Pressable
          onPress={() => router.back()}
          style={({ pressed }) => ({
            backgroundColor: pressed ? '#374151' : '#4B5563',
            borderRadius: 8,
            paddingVertical: 14,
            alignItems: 'center',
          })}
        >
          <Text style={{ color: '#FFFFFF', fontWeight: '600' }}>뒤로</Text>
        </Pressable>
      </ScrollView>
    );
  }

  const acc = account.data;
  const isActive = acc.status === 'ACTIVE';

  const handleDeposit = () => {
    if (!depositValidation || !depositValidation.ok) return;
    depositMut.mutate(
      { accountNumber: acc.accountNumber, body: { amount: depositValidation.value } },
      { onSuccess: () => setDepositAmount('') },
    );
  };

  const handleWithdraw = () => {
    if (!withdrawValidation || !withdrawValidation.ok) return;
    withdrawMut.mutate(
      { accountNumber: acc.accountNumber, body: { amount: withdrawValidation.value } },
      { onSuccess: () => setWithdrawAmount('') },
    );
  };

  const confirmClose = () => {
    const proceed = () => closeMut.mutate(acc.accountNumber);
    if (Platform.OS === 'web') {
      if (typeof window !== 'undefined' && window.confirm('정말 이 계좌를 해지하시겠어요?')) {
        proceed();
      }
      return;
    }
    Alert.alert('계좌 해지', '정말 이 계좌를 해지하시겠어요?', [
      { text: '취소', style: 'cancel' },
      { text: '해지', style: 'destructive', onPress: proceed },
    ]);
  };

  return (
    <ScrollView contentContainerStyle={{ padding: 20, gap: 20 }}>
      <AccountCard account={acc} />

      {depositMut.error && <ErrorBanner error={depositMut.error} />}
      {withdrawMut.error && <ErrorBanner error={withdrawMut.error} />}
      {closeMut.error && <ErrorBanner error={closeMut.error} />}

      {isActive && (
        <>
          <View style={{ gap: 10, backgroundColor: '#FFFFFF', padding: 16, borderRadius: 12 }}>
            <Text style={{ fontSize: 16, fontWeight: '700' }}>입금</Text>
            <MoneyInput
              value={depositAmount}
              onChangeText={setDepositAmount}
              disabled={depositMut.isPending}
            />
            <Pressable
              onPress={handleDeposit}
              disabled={depositMut.isPending || !(depositValidation && depositValidation.ok)}
              style={({ pressed }) => ({
                backgroundColor:
                  depositMut.isPending || !(depositValidation && depositValidation.ok)
                    ? '#9CA3AF'
                    : pressed
                    ? '#1D4ED8'
                    : '#2563EB',
                borderRadius: 8,
                paddingVertical: 14,
                alignItems: 'center',
              })}
            >
              <Text style={{ color: '#FFFFFF', fontWeight: '700', fontSize: 16 }}>
                {depositMut.isPending ? '입금 중…' : '입금하기'}
              </Text>
            </Pressable>
          </View>

          <View style={{ gap: 10, backgroundColor: '#FFFFFF', padding: 16, borderRadius: 12 }}>
            <Text style={{ fontSize: 16, fontWeight: '700' }}>출금</Text>
            <MoneyInput
              value={withdrawAmount}
              onChangeText={setWithdrawAmount}
              disabled={withdrawMut.isPending}
            />
            <Pressable
              onPress={handleWithdraw}
              disabled={withdrawMut.isPending || !(withdrawValidation && withdrawValidation.ok)}
              style={({ pressed }) => ({
                backgroundColor:
                  withdrawMut.isPending || !(withdrawValidation && withdrawValidation.ok)
                    ? '#9CA3AF'
                    : pressed
                    ? '#B45309'
                    : '#D97706',
                borderRadius: 8,
                paddingVertical: 14,
                alignItems: 'center',
              })}
            >
              <Text style={{ color: '#FFFFFF', fontWeight: '700', fontSize: 16 }}>
                {withdrawMut.isPending ? '출금 중…' : '출금하기'}
              </Text>
            </Pressable>
          </View>

          <Pressable
            onPress={() => router.push(`/accounts/${encodeURIComponent(acc.accountNumber)}/transfer`)}
            style={({ pressed }) => ({
              backgroundColor: pressed ? '#1E3A8A' : '#1E40AF',
              borderRadius: 8,
              paddingVertical: 14,
              alignItems: 'center',
            })}
          >
            <Text style={{ color: '#FFFFFF', fontWeight: '700', fontSize: 16 }}>→ 이체하기</Text>
          </Pressable>

          <Pressable
            onPress={confirmClose}
            disabled={closeMut.isPending}
            style={({ pressed }) => ({
              backgroundColor: closeMut.isPending
                ? '#9CA3AF'
                : pressed
                ? '#991B1B'
                : '#DC2626',
              borderRadius: 8,
              paddingVertical: 14,
              alignItems: 'center',
            })}
          >
            <Text style={{ color: '#FFFFFF', fontWeight: '700', fontSize: 16 }}>
              {closeMut.isPending ? '해지 중…' : '계좌 해지'}
            </Text>
          </Pressable>
        </>
      )}

      {!isActive && (
        <View
          style={{
            backgroundColor: '#F3F4F6',
            padding: 16,
            borderRadius: 12,
            alignItems: 'center',
          }}
        >
          <Text style={{ color: '#6B7280' }}>해지된 계좌는 추가 거래가 불가합니다.</Text>
        </View>
      )}

      <View style={{ gap: 8 }}>
        <Text style={{ fontSize: 16, fontWeight: '700' }}>최근 거래 내역</Text>
        {transactions.isLoading && <Text style={{ color: '#6B7280' }}>불러오는 중…</Text>}
        {transactions.error && <ErrorBanner error={transactions.error} />}
        {transactions.data && <TransactionList items={transactions.data} />}
      </View>
    </ScrollView>
  );
}
