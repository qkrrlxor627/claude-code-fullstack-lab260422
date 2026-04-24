import { apiClient } from './client';
import { TransferRequest, TransferResponse } from './types';

export async function transfer(
  body: TransferRequest,
  idempotencyKey: string,
): Promise<TransferResponse> {
  const res = await apiClient.post<TransferResponse>('/transfers', body, {
    headers: { 'X-Idempotency-Key': idempotencyKey },
  });
  return res.data;
}
