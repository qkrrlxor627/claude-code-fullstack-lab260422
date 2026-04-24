export type AccountType = 'SAVINGS' | 'FIXED_DEPOSIT' | 'LOAN';
export type AccountStatus = 'ACTIVE' | 'DORMANT' | 'CLOSED';

export interface AccountResponse {
  accountNumber: string;
  holder: string;
  type: AccountType;
  status: AccountStatus;
  balance: string;
  openedAt: string;
  closedAt: string | null;
}

export interface OpenAccountRequest {
  holder: string;
  type: AccountType;
}

export interface DepositRequest {
  amount: string;
}

export interface PingResponse {
  service: string;
  timestamp: string;
}

export type TransactionType = 'DEPOSIT' | 'WITHDRAWAL' | 'TRANSFER_OUT' | 'TRANSFER_IN';

export interface TransactionResponse {
  id: number;
  accountNumber: string;
  type: TransactionType;
  amount: string;
  counterpartyAccount: string | null;
  transferId: string | null;
  balanceAfter: string;
  createdAt: string;
}

export const TRANSACTION_TYPE_LABELS: Record<TransactionType, string> = {
  DEPOSIT: '입금',
  WITHDRAWAL: '출금',
  TRANSFER_OUT: '이체 (송금)',
  TRANSFER_IN: '이체 (수금)',
};

export interface TransferRequest {
  fromAccount: string;
  toAccount: string;
  amount: string;
}

export interface TransferResponse {
  transferId: string;
  fromAccount: string;
  toAccount: string;
  amount: string;
  fromBalanceAfter: string;
  toBalanceAfter: string;
  executedAt: string;
}

export type LedgerSide = 'DEBIT' | 'CREDIT';

export interface LedgerEntryResponse {
  id: number;
  transactionId: number;
  accountCode: string;
  side: LedgerSide;
  amount: string;
  memo: string | null;
  createdAt: string;
}

export interface ApiErrorPayload {
  code: string;
  message: string;
}

export interface ApiEnvelope<T> {
  success: boolean;
  data: T | null;
  error: ApiErrorPayload | null;
  timestamp: string;
}

export class ApiError extends Error {
  readonly code: string;
  readonly status: number;

  constructor(code: string, message: string, status: number) {
    super(message);
    this.name = 'ApiError';
    this.code = code;
    this.status = status;
  }
}

export const ACCOUNT_TYPE_LABELS: Record<AccountType, string> = {
  SAVINGS: '보통예금',
  FIXED_DEPOSIT: '정기예금',
  LOAN: '대출계좌',
};

export const ACCOUNT_STATUS_LABELS: Record<AccountStatus, string> = {
  ACTIVE: '정상',
  DORMANT: '휴면',
  CLOSED: '해지',
};
