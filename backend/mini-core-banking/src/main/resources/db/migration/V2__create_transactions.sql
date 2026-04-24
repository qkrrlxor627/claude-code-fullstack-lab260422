-- Phase 2 Step 2: 거래(Transaction) 엔티티
-- 모든 입금/출금/이체는 이 테이블에 한 건(이체는 2건, OUT/IN) 이상 기록된다.
-- 잔액 직접 UPDATE 금지 원칙의 근거이자 감사 추적 소스.

CREATE TABLE transactions (
    id                    BIGSERIAL      PRIMARY KEY,
    account_number        VARCHAR(20)    NOT NULL,
    type                  VARCHAR(20)    NOT NULL,
    amount                NUMERIC(18, 2) NOT NULL,
    counterparty_account  VARCHAR(20),
    transfer_id           VARCHAR(36),
    balance_after         NUMERIC(18, 2) NOT NULL,
    created_at            TIMESTAMPTZ    NOT NULL
);

COMMENT ON TABLE  transactions IS '거래(입금/출금/이체) 이력';
COMMENT ON COLUMN transactions.type IS 'DEPOSIT / WITHDRAWAL / TRANSFER_OUT / TRANSFER_IN';
COMMENT ON COLUMN transactions.counterparty_account IS '이체 상대 계좌 (이체일 때만 NOT NULL)';
COMMENT ON COLUMN transactions.transfer_id IS '이체 그룹 UUID (OUT 과 IN 을 묶는 키, 이체일 때만 NOT NULL)';
COMMENT ON COLUMN transactions.balance_after IS '거래 직후 해당 계좌 잔액 (감사 + UI 표시 최적화)';

-- 계좌별 최근 거래 조회용
CREATE INDEX idx_tx_account_created ON transactions(account_number, created_at DESC);
-- 이체 그룹 조회용 (transfer_id 로 OUT/IN 같이 가져올 때)
CREATE INDEX idx_tx_transfer ON transactions(transfer_id) WHERE transfer_id IS NOT NULL;
