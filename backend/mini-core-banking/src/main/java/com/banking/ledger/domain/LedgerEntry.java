package com.banking.ledger.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

/**
 * 원장 엔트리 (불변).
 * 한 거래(Transaction)에 대해 DEBIT / CREDIT 최소 2건이 생성되어야 차·대변이 맞는다.
 */
public final class LedgerEntry {

    public static final String CASH_ASSET = "CASH_ASSET";

    private final Long id;
    private final Long transactionId;
    private final String accountCode;
    private final LedgerSide side;
    private final BigDecimal amount;
    private final String memo;
    private final Instant createdAt;

    private LedgerEntry(Long id, Long transactionId, String accountCode, LedgerSide side,
                        BigDecimal amount, String memo, Instant createdAt) {
        this.id = id;
        this.transactionId = Objects.requireNonNull(transactionId, "transactionId");
        this.accountCode = Objects.requireNonNull(accountCode, "accountCode");
        this.side = Objects.requireNonNull(side, "side");
        BigDecimal scaled = Objects.requireNonNull(amount, "amount").setScale(2, RoundingMode.HALF_UP);
        if (scaled.signum() <= 0) {
            throw new IllegalArgumentException("원장 금액은 0보다 커야 합니다: " + scaled);
        }
        this.amount = scaled;
        this.memo = memo;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    public static LedgerEntry create(Long transactionId, String accountCode, LedgerSide side,
                                     BigDecimal amount, String memo, Clock clock) {
        return new LedgerEntry(null, transactionId, accountCode, side, amount, memo, clock.instant());
    }

    public static LedgerEntry reconstitute(Long id, Long transactionId, String accountCode,
                                           LedgerSide side, BigDecimal amount, String memo,
                                           Instant createdAt) {
        return new LedgerEntry(id, transactionId, accountCode, side, amount, memo, createdAt);
    }

    public LedgerEntry withId(Long newId) {
        if (this.id != null) throw new IllegalStateException("이미 id 가 할당됨: " + this.id);
        return new LedgerEntry(Objects.requireNonNull(newId), transactionId, accountCode,
                side, amount, memo, createdAt);
    }

    public Long id() { return id; }
    public Long transactionId() { return transactionId; }
    public String accountCode() { return accountCode; }
    public LedgerSide side() { return side; }
    public BigDecimal amount() { return amount; }
    public String memo() { return memo; }
    public Instant createdAt() { return createdAt; }
}
