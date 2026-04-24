import { useMutation, useQueryClient } from '@tanstack/react-query';
import { transfer } from '@/api/transfers';
import { ApiError, TransferRequest, TransferResponse } from '@/api/types';
import { accountQueryKey } from './useAccount';

interface Vars {
  body: TransferRequest;
  idempotencyKey: string;
}

export function useTransfer() {
  const qc = useQueryClient();
  return useMutation<TransferResponse, ApiError, Vars>({
    mutationFn: ({ body, idempotencyKey }) => transfer(body, idempotencyKey),
    onSuccess: (response) => {
      qc.invalidateQueries({ queryKey: accountQueryKey(response.fromAccount) });
      qc.invalidateQueries({ queryKey: accountQueryKey(response.toAccount) });
      qc.invalidateQueries({ queryKey: ['transactions', response.fromAccount] });
      qc.invalidateQueries({ queryKey: ['transactions', response.toAccount] });
    },
  });
}

export function newIdempotencyKey(): string {
  if (typeof globalThis.crypto?.randomUUID === 'function') {
    return globalThis.crypto.randomUUID();
  }
  // Fallback — RFC4122 v4 식, non-crypto
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}
