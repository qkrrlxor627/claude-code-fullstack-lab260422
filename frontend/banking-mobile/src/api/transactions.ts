import { apiClient } from './client';
import { TransactionResponse } from './types';

export async function listTransactions(
  accountNumber: string,
  limit = 20,
): Promise<TransactionResponse[]> {
  const res = await apiClient.get<TransactionResponse[]>(
    `/accounts/${encodeURIComponent(accountNumber)}/transactions`,
    { params: { limit } },
  );
  return res.data;
}
