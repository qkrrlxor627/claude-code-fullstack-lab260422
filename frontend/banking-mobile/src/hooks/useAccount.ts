import { useQuery } from '@tanstack/react-query';
import { getAccount } from '@/api/accounts';
import { AccountResponse, ApiError } from '@/api/types';

export const accountQueryKey = (accountNumber: string) => ['account', accountNumber] as const;

export function useAccount(accountNumber: string | undefined) {
  return useQuery<AccountResponse, ApiError>({
    queryKey: accountQueryKey(accountNumber ?? ''),
    queryFn: () => getAccount(accountNumber as string),
    enabled: Boolean(accountNumber),
    retry: (failureCount, error) => {
      if (error instanceof ApiError && error.code === 'ACCOUNT_NOT_FOUND') return false;
      return failureCount < 2;
    },
  });
}
