package com.banking.transaction.infra;

import com.banking.transaction.domain.TransactionType;
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

/**
 * transactions 테이블 매핑. 도메인 Transaction 과 분리.
 */
@Entity
@Table(name = "transactions")
public class TransactionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", nullable = false, length = 20)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "counterparty_account", length = 20)
    private String counterpartyAccount;

    @Column(name = "transfer_id", length = 36)
    private String transferId;

    @Column(name = "balance_after", nullable = false, precision = 18, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TransactionJpaEntity() {
        // JPA
    }

    public TransactionJpaEntity(Long id, String accountNumber, TransactionType type,
                                BigDecimal amount, String counterpartyAccount, String transferId,
                                BigDecimal balanceAfter, Instant createdAt) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.counterpartyAccount = counterpartyAccount;
        this.transferId = transferId;
        this.balanceAfter = balanceAfter;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getCounterpartyAccount() { return counterpartyAccount; }
    public String getTransferId() { return transferId; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public Instant getCreatedAt() { return createdAt; }
}
