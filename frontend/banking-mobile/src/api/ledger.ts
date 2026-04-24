import { apiClient } from './client';
import { LedgerEntryResponse } from './types';

export async function listLedgerByAccount(
  accountCode: string,
  limit = 50,
): Promise<LedgerEntryResponse[]> {
  const res = await apiClient.get<LedgerEntryResponse[]>('/ledger', {
    params: { accountCode, limit },
  });
  return res.data;
}
