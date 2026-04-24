import { Link, useRouter } from 'expo-router';
import { useState } from 'react';
import { Pressable, ScrollView, Text, TextInput, View } from 'react-native';
import { usePing } from '@/hooks/usePing';
import { normalizeAccountNumberInput } from '@/utils/accountNumber';

export default function HomeScreen() {
  const router = useRouter();
  const [accountNumber, setAccountNumber] = useState('');
  const ping = usePing();

  const canLookup = accountNumber.trim().length > 0;

  const handleLookup = () => {
    if (!canLookup) return;
    const normalized = normalizeAccountNumberInput(accountNumber);
    router.push(`/accounts/${encodeURIComponent(normalized)}`);
  };

  return (
    <ScrollView contentContainerStyle={{ padding: 20, gap: 24 }}>
      <ServerStatusRow
        ok={ping.isSuccess}
        loading={ping.isLoading}
        message={
          ping.isSuccess
            ? `연결됨 · ${ping.data?.service ?? 'mini-core-banking'}`
            : ping.isLoading
            ? '서버 확인 중…'
            : '서버 연결 실패'
        }
      />

      <View style={{ gap: 8 }}>
        <Text style={{ fontSize: 14, color: '#6B7280' }}>계좌번호로 조회</Text>
        <TextInput
          value={accountNumber}
          onChangeText={setAccountNumber}
          placeholder="110-01-1000000-X"
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
        <Pressable
          onPress={handleLookup}
          disabled={!canLookup}
          style={({ pressed }) => ({
            backgroundColor: canLookup ? (pressed ? '#1D4ED8' : '#2563EB') : '#9CA3AF',
            borderRadius: 8,
            paddingVertical: 14,
            alignItems: 'center',
          })}
        >
          <Text style={{ color: '#FFFFFF', fontWeight: '700', fontSize: 16 }}>조회</Text>
        </Pressable>
      </View>

      <View style={{ height: 1, backgroundColor: '#E5E7EB' }} />

      <View style={{ gap: 8 }}>
        <Link href="/accounts/new" asChild>
          <Pressable
            style={({ pressed }) => ({
              backgroundColor: pressed ? '#047857' : '#059669',
              borderRadius: 8,
              paddingVertical: 16,
              alignItems: 'center',
            })}
          >
            <Text style={{ color: '#FFFFFF', fontWeight: '700', fontSize: 16 }}>+ 새 계좌 개설</Text>
          </Pressable>
        </Link>

        <Link href="/settings" asChild>
          <Pressable
            style={({ pressed }) => ({
              backgroundColor: 'transparent',
              paddingVertical: 10,
              alignItems: 'center',
              opacity: pressed ? 0.6 : 1,
            })}
          >
            <Text style={{ color: '#6B7280', fontSize: 13 }}>API 설정 확인</Text>
          </Pressable>
        </Link>
      </View>
    </ScrollView>
  );
}

function ServerStatusRow({
  ok,
  loading,
  message,
}: {
  ok: boolean;
  loading: boolean;
  message: string;
}) {
  const color = ok ? '#10B981' : loading ? '#F59E0B' : '#EF4444';
  return (
    <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
      <View
        style={{
          width: 10,
          height: 10,
          borderRadius: 5,
          backgroundColor: color,
        }}
      />
      <Text style={{ color: '#374151', fontSize: 13 }}>{message}</Text>
    </View>
  );
}
