import { useMemo } from 'react';
import { Text, TextInput, View } from 'react-native';
import { validateAmount } from '@/utils/money';

interface Props {
  value: string;
  onChangeText: (v: string) => void;
  placeholder?: string;
  disabled?: boolean;
}

export function MoneyInput({ value, onChangeText, placeholder = '0', disabled }: Props) {
  const validation = useMemo(() => {
    if (value.trim() === '') return null;
    return validateAmount(value);
  }, [value]);

  return (
    <View style={{ gap: 4 }}>
      <View
        style={{
          flexDirection: 'row',
          alignItems: 'center',
          borderWidth: 1,
          borderColor: validation && !validation.ok ? '#FCA5A5' : '#D1D5DB',
          borderRadius: 8,
          paddingHorizontal: 12,
          backgroundColor: disabled ? '#F3F4F6' : '#FFFFFF',
        }}
      >
        <Text style={{ fontSize: 16, color: '#6B7280', marginRight: 6 }}>₩</Text>
        <TextInput
          editable={!disabled}
          value={value}
          onChangeText={onChangeText}
          placeholder={placeholder}
          keyboardType="decimal-pad"
          inputMode="decimal"
          style={{
            flex: 1,
            paddingVertical: 12,
            fontSize: 18,
            fontVariant: ['tabular-nums'],
          }}
        />
      </View>
      {validation && !validation.ok && (
        <Text style={{ color: '#B91C1C', fontSize: 12 }}>{validation.reason}</Text>
      )}
    </View>
  );
}
