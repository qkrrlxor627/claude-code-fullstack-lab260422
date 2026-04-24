import { apiClient } from './client';
import {
  AccountResponse,
  DepositRequest,
  OpenAccountRequest,
} from './types';

export async function openAccount(body: OpenAccountRequest): Promise<AccountResponse> {
  const res = await apiClient.post<AccountResponse>('/accounts', body);
  return res.data;
}

export async function getAccount(accountNumber: string): Promise<AccountResponse> {
  const res = await apiClient.get<AccountResponse>(`/accounts/${encodeURIComponent(accountNumber)}`);
  return res.data;
}

export async function deposit(
  accountNumber: string,
  body: DepositRequest,
): Promise<AccountResponse> {
  const res = await apiClient.post<AccountResponse>(
    `/accounts/${encodeURIComponent(accountNumber)}/deposits`,
    body,
  );
  return res.data;
}

export async function withdraw(
  accountNumber: string,
  body: DepositRequest,
): Promise<AccountResponse> {
  const res = await apiClient.post<AccountResponse>(
    `/accounts/${encodeURIComponent(accountNumber)}/withdrawals`,
    body,
  );
  return res.data;
}

export async function closeAccount(accountNumber: string): Promise<AccountResponse> {
  const res = await apiClient.delete<AccountResponse>(`/accounts/${encodeURIComponent(accountNumber)}`);
  return res.data;
}
