package com.banking.infra.persistence.account;

import com.banking.domain.account.AccountStatus;
import com.banking.domain.account.AccountType;
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
 * accounts 테이블 매핑 JPA 엔티티.
 * 도메인 Account 와 분리 — 변환은 {@link AccountMapper}.
 * 스키마는 Flyway(V1__create_accounts.sql)가 관리하고, JPA 는 validate 만 수행.
 */
@Entity
@Table(name = "accounts")
class AccountJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;

    @Column(nullable = false, length = 50)
    private String holder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal balance;

    @Column(name = "opened_at", nullable = false)
    private Instant openedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    protected AccountJpaEntity() {
        // JPA
    }

    AccountJpaEntity(Long id, String accountNumber, String holder, AccountType type,
                     AccountStatus status, BigDecimal balance,
                     Instant openedAt, Instant closedAt) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.holder = holder;
        this.type = type;
        this.status = status;
        this.balance = balance;
        this.openedAt = openedAt;
        this.closedAt = closedAt;
    }

    Long getId() { return id; }
    String getAccountNumber() { return accountNumber; }
    String getHolder() { return holder; }
    AccountType getType() { return type; }
    AccountStatus getStatus() { return status; }
    BigDecimal getBalance() { return balance; }
    Instant getOpenedAt() { return openedAt; }
    Instant getClosedAt() { return closedAt; }
}
