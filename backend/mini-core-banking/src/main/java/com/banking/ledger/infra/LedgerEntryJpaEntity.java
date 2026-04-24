package com.banking.ledger.infra;

import com.banking.ledger.domain.LedgerSide;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;

    @Column(name = "account_code", nullable = false, length = 20)
    private String accountCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private LedgerSide side;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(length = 100)
    private String memo;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected LedgerEntryJpaEntity() {}

    public LedgerEntryJpaEntity(Long id, Long transactionId, String accountCode, LedgerSide side,
                                BigDecimal amount, String memo, Instant createdAt) {
        this.id = id;
        this.transactionId = transactionId;
        this.accountCode = accountCode;
        this.side = side;
        this.amount = amount;
        this.memo = memo;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getTransactionId() { return transactionId; }
    public String getAccountCode() { return accountCode; }
    public LedgerSide getSide() { return side; }
    public BigDecimal getAmount() { return amount; }
    public String getMemo() { return memo; }
    public Instant getCreatedAt() { return createdAt; }
}
