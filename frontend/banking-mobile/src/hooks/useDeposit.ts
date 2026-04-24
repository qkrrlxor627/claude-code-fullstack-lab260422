import { useMutation, useQueryClient } from '@tanstack/react-query';
import { deposit } from '@/api/accounts';
import { AccountResponse, ApiError, DepositRequest } from '@/api/types';
import { accountQueryKey } from './useAccount';

interface Vars {
  accountNumber: string;
  body: DepositRequest;
}

export function useDeposit() {
  const qc = useQueryClient();
  return useMutation<AccountResponse, ApiError, Vars>({
    mutationFn: ({ accountNumber, body }) => deposit(accountNumber, body),
    onSuccess: (account) => {
      qc.setQueryData(accountQueryKey(account.accountNumber), account);
      qc.invalidateQueries({ queryKey: ['transactions', account.accountNumber] });
    },
  });
}
