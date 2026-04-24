import { zodResolver } from '@hookform/resolvers/zod';
import { useRouter } from 'expo-router';
import { Controller, useForm } from 'react-hook-form';
import { Pressable, ScrollView, Text, TextInput, View } from 'react-native';
import { z } from 'zod';
import { ACCOUNT_TYPE_LABELS, AccountType } from '@/api/types';
import { ErrorBanner } from '@/components/ErrorBanner';
import { useOpenAccount } from '@/hooks/useOpenAccount';

const schema = z.object({
  holder: z
    .string()
    .trim()
    .min(2, '예금주는 2자 이상 입력해주세요')
    .max(50, '예금주는 50자 이하로 입력해주세요'),
  type: z.enum(['SAVINGS', 'FIXED_DEPOSIT', 'LOAN']),
});

type FormValues = z.infer<typeof schema>;

const TYPE_OPTIONS: AccountType[] = ['SAVINGS', 'FIXED_DEPOSIT', 'LOAN'];

export default function NewAccountScreen() {
  const router = useRouter();
  const openAccount = useOpenAccount();

  const { control, handleSubmit, formState } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { holder: '', type: 'SAVINGS' },
  });

  const onSubmit = handleSubmit((values) => {
    openAccount.mutate(values, {
      onSuccess: (account) => {
        router.replace(`/accounts/${encodeURIComponent(account.accountNumber)}`);
      },
    });
  });

  return (
    <ScrollView contentContainerStyle={{ padding: 20, gap: 20 }}>
      {openAccount.error && <ErrorBanner error={openAccount.error} />}

      <View style={{ gap: 6 }}>
        <Text style={{ fontSize: 14, color: '#6B7280' }}>예금주 (2~50자)</Text>
        <Controller
          control={control}
          name="holder"
          render={({ field, fieldState }) => (
            <>
              <TextInput
                value={field.value}
                onChangeText={field.onChange}
                placeholder="김싸피"
                autoCorrect={false}
                style={{
                  borderWidth: 1,
                  borderColor: fieldState.error ? '#FCA5A5' : '#D1D5DB',
                  borderRadius: 8,
                  padding: 12,
                  fontSize: 16,
                  backgroundColor: '#FFFFFF',
                }}
              />
              {fieldState.error && (
                <Text style={{ color: '#B91C1C', fontSize: 12 }}>{fieldState.error.message}</Text>
              )}
            </>
          )}
        />
      </View>

      <View style={{ gap: 6 }}>
        <Text style={{ fontSize: 14, color: '#6B7280' }}>계좌 종류</Text>
        <Controller
          control={control}
          name="type"
          render={({ field }) => (
            <View style={{ flexDirection: 'row', gap: 8 }}>
              {TYPE_OPTIONS.map((t) => {
                const selected = field.value === t;
                return (
                  <Pressable
                    key={t}
                    onPress={() => field.onChange(t)}
                    style={({ pressed }) => ({
                      flex: 1,
                      paddingVertical: 12,
                      borderRadius: 8,
                      borderWidth: 1,
                      borderColor: selected ? '#2563EB' : '#D1D5DB',
                      backgroundColor: selected ? '#DBEAFE' : '#FFFFFF',
                      alignItems: 'center',
                      opacity: pressed ? 0.85 : 1,
                    })}
                  >
                    <Text
                      style={{
                        color: selected ? '#1E3A8A' : '#374151',
                        fontWeight: selected ? '700' : '500',
                      }}
                    >
                      {ACCOUNT_TYPE_LABELS[t]}
                    </Text>
                  </Pressable>
                );
              })}
            </View>
          )}
        />
      </View>

      <Pressable
        onPress={onSubmit}
        disabled={openAccount.isPending || !formState.isValid}
        style={({ pressed }) => ({
          backgroundColor:
            openAccount.isPending || !formState.isValid
              ? '#9CA3AF'
              : pressed
              ? '#047857'
              : '#059669',
          borderRadius: 8,
          paddingVertical: 16,
          alignItems: 'center',
          marginTop: 8,
        })}
      >
        <Text style={{ color: '#FFFFFF', fontWeight: '700', fontSize: 16 }}>
          {openAccount.isPending ? '개설 중…' : '계좌 개설하기'}
        </Text>
      </Pressable>
    </ScrollView>
  );
}
