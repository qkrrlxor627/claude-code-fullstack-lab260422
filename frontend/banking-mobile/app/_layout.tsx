import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { useMemo } from 'react';
import { SafeAreaProvider } from 'react-native-safe-area-context';

export default function RootLayout() {
  const queryClient = useMemo(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: {
            staleTime: 5_000,
            refetchOnWindowFocus: false,
          },
        },
      }),
    [],
  );

  return (
    <SafeAreaProvider>
      <QueryClientProvider client={queryClient}>
        <StatusBar style="dark" />
        <Stack
          screenOptions={{
            headerStyle: { backgroundColor: '#F9FAFB' },
            headerTitleStyle: { fontWeight: '700' },
            contentStyle: { backgroundColor: '#F9FAFB' },
          }}
        >
          <Stack.Screen name="index" options={{ title: 'Mini Core Banking' }} />
          <Stack.Screen name="accounts/new" options={{ title: '계좌 개설' }} />
          <Stack.Screen name="accounts/[accountNumber]/index" options={{ title: '계좌 상세' }} />
          <Stack.Screen name="accounts/[accountNumber]/transfer" options={{ title: '계좌 이체' }} />
          <Stack.Screen name="settings" options={{ title: '설정' }} />
          <Stack.Screen name="ledger" options={{ title: '원장 (관리자)' }} />
        </Stack>
      </QueryClientProvider>
    </SafeAreaProvider>
  );
}
