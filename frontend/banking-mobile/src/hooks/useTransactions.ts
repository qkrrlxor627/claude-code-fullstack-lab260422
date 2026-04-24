import { useQuery } from '@tanstack/react-query';
import { listTransactions } from '@/api/transactions';
import { ApiError, TransactionResponse } from '@/api/types';

export const transactionsQueryKey = (accountNumber: string, limit: number) =>
  ['transactions', accountNumber, limit] as const;

export function useTransactions(accountNumber: string | undefined, limit = 20) {
  return useQuery<TransactionResponse[], ApiError>({
    queryKey: transactionsQueryKey(accountNumber ?? '', limit),
    queryFn: () => listTransactions(accountNumber as string, limit),
    enabled: Boolean(accountNumber),
  });
}
