-- Phase 2 Step 2-1: 계좌 도메인
-- created_at / updated_at 컬럼은 감사(Audit) 요구사항이 생기는 단계에서 별도 마이그레이션으로 추가.

CREATE TABLE accounts (
    id              BIGSERIAL       PRIMARY KEY,
    account_number  VARCHAR(20)     NOT NULL UNIQUE,
    holder          VARCHAR(50)     NOT NULL,
    type            VARCHAR(20)     NOT NULL,
    status          VARCHAR(20)     NOT NULL,
    balance         NUMERIC(18, 2)  NOT NULL DEFAULT 0,
    opened_at       TIMESTAMPTZ     NOT NULL,
    closed_at       TIMESTAMPTZ
);

COMMENT ON TABLE  accounts IS '고객 계좌';
COMMENT ON COLUMN accounts.account_number IS '계좌번호 (형식: NNN-NN-NNNNNNN-N, Luhn 검증번호 포함)';
COMMENT ON COLUMN accounts.type   IS '상품 유형: SAVINGS / FIXED_DEPOSIT / LOAN';
COMMENT ON COLUMN accounts.status IS '계좌 상태: ACTIVE / DORMANT / CLOSED';
COMMENT ON COLUMN accounts.balance IS '잔액 (원, precision=18 scale=2)';

-- 일련번호 채번 시퀀스 (AccountNumber.generate 에서 사용)
CREATE SEQUENCE account_number_seq
    START WITH 1000000
    INCREMENT BY 1
    NO MAXVALUE
    NO CYCLE;

-- 상태별 조회 대비 (Phase 5 휴면/해지 계좌 조회)
CREATE INDEX idx_accounts_status ON accounts(status);
