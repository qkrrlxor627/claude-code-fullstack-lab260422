import { apiClient } from './client';
import { PingResponse } from './types';

export async function ping(): Promise<PingResponse> {
  const res = await apiClient.get<PingResponse>('/ping');
  return res.data;
}
