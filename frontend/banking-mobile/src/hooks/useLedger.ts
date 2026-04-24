import { useQuery } from '@tanstack/react-query';
import { listLedgerByAccount } from '@/api/ledger';
import { ApiError, LedgerEntryResponse } from '@/api/types';

export function useLedger(accountCode: string | undefined, limit = 50) {
  return useQuery<LedgerEntryResponse[], ApiError>({
    queryKey: ['ledger', accountCode ?? '', limit],
    queryFn: () => listLedgerByAccount(accountCode as string, limit),
    enabled: Boolean(accountCode),
  });
}
