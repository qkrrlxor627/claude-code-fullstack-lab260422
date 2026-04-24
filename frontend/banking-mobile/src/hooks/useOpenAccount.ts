import { useMutation, useQueryClient } from '@tanstack/react-query';
import { openAccount } from '@/api/accounts';
import { AccountResponse, ApiError, OpenAccountRequest } from '@/api/types';
import { accountQueryKey } from './useAccount';

export function useOpenAccount() {
  const qc = useQueryClient();
  return useMutation<AccountResponse, ApiError, OpenAccountRequest>({
    mutationFn: openAccount,
    onSuccess: (account) => {
      qc.setQueryData(accountQueryKey(account.accountNumber), account);
    },
  });
}
