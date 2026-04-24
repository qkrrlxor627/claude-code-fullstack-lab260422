import { useMutation, useQueryClient } from '@tanstack/react-query';
import { withdraw } from '@/api/accounts';
import { AccountResponse, ApiError, DepositRequest } from '@/api/types';
import { accountQueryKey } from './useAccount';

interface Vars {
  accountNumber: string;
  body: DepositRequest;
}

export function useWithdraw() {
  const qc = useQueryClient();
  return useMutation<AccountResponse, ApiError, Vars>({
    mutationFn: ({ accountNumber, body }) => withdraw(accountNumber, body),
    onSuccess: (account) => {
      qc.setQueryData(accountQueryKey(account.accountNumber), account);
      qc.invalidateQueries({ queryKey: ['transactions', account.accountNumber] });
    },
  });
}
