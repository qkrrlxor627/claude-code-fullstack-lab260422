package com.banking.transaction.domain;

import com.banking.account.domain.AccountNumber;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

/**
 * 거래 이력 도메인 엔티티 (불변).
 * 생성 이후 어떤 필드도 변경할 수 없다. 수정/삭제는 원장 원칙 위반.
 */
public final class Transaction {

    private final Long id;
    private final AccountNumber accountNumber;
    private final TransactionType type;
    private final BigDecimal amount;
    private final AccountNumber counterparty;
    private final String transferId;
    private final BigDecimal balanceAfter;
    private final Instant createdAt;

    private Transaction(Long id, AccountNumber accountNumber, TransactionType type,
                        BigDecimal amount, AccountNumber counterparty, String transferId,
                        BigDecimal balanceAfter, Instant createdAt) {
        this.id = id;
        this.accountNumber = Objects.requireNonNull(accountNumber, "accountNumber");
        this.type = Objects.requireNonNull(type, "type");
        this.amount = Objects.requireNonNull(amount, "amount").setScale(2, RoundingMode.HALF_UP);
        this.counterparty = counterparty;
        this.transferId = transferId;
        this.balanceAfter = Objects.requireNonNull(balanceAfter, "balanceAfter")
                .setScale(2, RoundingMode.HALF_UP);
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    /** 신규 입금/출금 거래. counterparty / transferId 는 null. */
    public static Transaction singleParty(AccountNumber account, TransactionType type,
                                          BigDecimal amount, BigDecimal balanceAfter, Clock clock) {
        if (type == TransactionType.TRANSFER_OUT || type == TransactionType.TRANSFER_IN) {
            throw new IllegalArgumentException("이체 거래는 transfer() 를 사용");
        }
        return new Transaction(null, account, type, amount, null, null, balanceAfter, clock.instant());
    }

    /** 이체 쪽 거래 (OUT 또는 IN). counterparty + transferId 필수. */
    public static Transaction transfer(AccountNumber account, TransactionType type,
                                       BigDecimal amount, AccountNumber counterparty,
                                       String transferId, BigDecimal balanceAfter, Clock clock) {
        if (type != TransactionType.TRANSFER_OUT && type != TransactionType.TRANSFER_IN) {
            throw new IllegalArgumentException("transfer 는 TRANSFER_OUT / TRANSFER_IN 만 허용");
        }
        Objects.requireNonNull(counterparty, "counterparty");
        Objects.requireNonNull(transferId, "transferId");
        return new Transaction(null, account, type, amount, counterparty, transferId,
                balanceAfter, clock.instant());
    }

    /** 저장소에서 재구성. */
    public static Transaction reconstitute(Long id, AccountNumber account, TransactionType type,
                                           BigDecimal amount, AccountNumber counterparty,
                                           String transferId, BigDecimal balanceAfter, Instant createdAt) {
        return new Transaction(id, account, type, amount, counterparty, transferId,
                balanceAfter, createdAt);
    }

    public Transaction withId(Long newId) {
        if (this.id != null) throw new IllegalStateException("이미 id 가 할당됨: " + this.id);
        return new Transaction(Objects.requireNonNull(newId), accountNumber, type, amount,
                counterparty, transferId, balanceAfter, createdAt);
    }

    public Long id() { return id; }
    public AccountNumber accountNumber() { return accountNumber; }
    public TransactionType type() { return type; }
    public BigDecimal amount() { return amount; }
    public AccountNumber counterparty() { return counterparty; }
    public String transferId() { return transferId; }
    public BigDecimal balanceAfter() { return balanceAfter; }
    public Instant createdAt() { return createdAt; }
}
