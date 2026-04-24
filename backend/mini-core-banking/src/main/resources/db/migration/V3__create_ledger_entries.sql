-- Phase 3: 원장 (LedgerEntry) — 복식부기
-- 모든 거래는 원장에 2건 이상의 차변/대변 엔트리로 기록된다.
-- 한 transaction 의 차변 합 = 대변 합 = 거래 금액.
-- LedgerEntry 는 불변 (수정/삭제 금지). 회계 감사 원칙.

CREATE TABLE ledger_entries (
    id              BIGSERIAL       PRIMARY KEY,
    transaction_id  BIGINT          NOT NULL,
    account_code    VARCHAR(20)     NOT NULL,
    side            VARCHAR(10)     NOT NULL,
    amount          NUMERIC(18, 2)  NOT NULL,
    memo            VARCHAR(100),
    created_at      TIMESTAMPTZ     NOT NULL,

    CONSTRAINT chk_ledger_side CHECK (side IN ('DEBIT', 'CREDIT')),
    CONSTRAINT chk_ledger_amount_positive CHECK (amount > 0)
);

COMMENT ON TABLE  ledger_entries IS '원장(복식부기) 엔트리. 불변';
COMMENT ON COLUMN ledger_entries.transaction_id IS '원본 거래 ID (transactions.id)';
COMMENT ON COLUMN ledger_entries.account_code IS '원장 계정 코드: 실계좌번호 또는 CASH_ASSET 같은 내부 계정';
COMMENT ON COLUMN ledger_entries.side IS 'DEBIT (차변) / CREDIT (대변)';

CREATE INDEX idx_ledger_tx   ON ledger_entries(transaction_id);
CREATE INDEX idx_ledger_acct ON ledger_entries(account_code, created_at DESC);
