import { useMutation, useQueryClient } from '@tanstack/react-query';
import { closeAccount } from '@/api/accounts';
import { AccountResponse, ApiError } from '@/api/types';
import { accountQueryKey } from './useAccount';

export function useCloseAccount() {
  const qc = useQueryClient();
  return useMutation<AccountResponse, ApiError, string>({
    mutationFn: (accountNumber) => closeAccount(accountNumber),
    onSuccess: (account) => {
      qc.setQueryData(accountQueryKey(account.accountNumber), account);
    },
  });
}
