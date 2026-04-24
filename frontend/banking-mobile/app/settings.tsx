import { Link } from 'expo-router';
import { Pressable, ScrollView, Text, View } from 'react-native';
import { getApiBaseUrl } from '@/api/client';
import { ErrorBanner } from '@/components/ErrorBanner';
import { usePing } from '@/hooks/usePing';

export default function SettingsScreen() {
  const ping = usePing();

  return (
    <ScrollView contentContainerStyle={{ padding: 20, gap: 16 }}>
      <View style={{ gap: 4 }}>
        <Text style={{ fontSize: 13, color: '#6B7280' }}>EXPO_PUBLIC_API_BASE_URL</Text>
        <Text
          style={{
            fontSize: 14,
            fontFamily: 'Menlo',
            backgroundColor: '#FFFFFF',
            borderWidth: 1,
            borderColor: '#E5E7EB',
            padding: 12,
            borderRadius: 8,
          }}
        >
          {getApiBaseUrl()}
        </Text>
        <Text style={{ fontSize: 12, color: '#9CA3AF' }}>
          변경하려면 `.env` 파일의 EXPO_PUBLIC_API_BASE_URL 을 수정 후 Expo 서버를 재시작하세요.
        </Text>
      </View>

      <View style={{ gap: 4 }}>
        <Text style={{ fontSize: 13, color: '#6B7280' }}>핑 응답</Text>
        {ping.isLoading && <Text>확인 중…</Text>}
        {ping.isError && <ErrorBanner error={ping.error} />}
        {ping.isSuccess && (
          <View
            style={{
              backgroundColor: '#ECFDF5',
              borderWidth: 1,
              borderColor: '#A7F3D0',
              borderRadius: 8,
              padding: 12,
            }}
          >
            <Text style={{ fontFamily: 'Menlo', fontSize: 12 }}>
              service: {ping.data.service}
            </Text>
            <Text style={{ fontFamily: 'Menlo', fontSize: 12 }}>
              timestamp: {ping.data.timestamp}
            </Text>
          </View>
        )}
      </View>

      <View style={{ height: 1, backgroundColor: '#E5E7EB' }} />

      <Link href="/ledger" asChild>
        <Pressable
          style={({ pressed }) => ({
            backgroundColor: pressed ? '#374151' : '#4B5563',
            borderRadius: 8,
            paddingVertical: 14,
            alignItems: 'center',
          })}
        >
          <Text style={{ color: '#FFFFFF', fontWeight: '700' }}>원장 보기 (관리자)</Text>
        </Pressable>
      </Link>
    </ScrollView>
  );
}
