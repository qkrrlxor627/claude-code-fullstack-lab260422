package com.banking.domain.account;

import com.banking.domain.account.exception.AccountAlreadyClosedException;
import com.banking.domain.account.exception.AccountNotEmptyException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

/**
 * 계좌 도메인 엔티티 (순수 Java, JPA 비의존).
 *
 * 설계 원칙:
 * - 정적 팩토리 {@link #open} 으로만 신규 개설.
 * - JPA 에서 재구성할 때는 {@link #reconstitute} 를 사용.
 * - 직접적인 setter 는 노출하지 않고, 상태 변경은 도메인 메서드({@link #close}) 로만.
 * - 잔액은 항상 scale=2 로 고정 (RoundingMode.HALF_UP).
 * - deposit / withdraw 는 Step 2-2 에서 추가.
 */
public class Account {

    private Long id;
    private final AccountNumber accountNumber;
    private final String holder;
    private final AccountType type;
    private AccountStatus status;
    private BigDecimal balance;
    private final Instant openedAt;
    private Instant closedAt;

    private Account(Long id, AccountNumber accountNumber, String holder, AccountType type,
                    AccountStatus status, BigDecimal balance,
                    Instant openedAt, Instant closedAt) {
        this.id = id;
        this.accountNumber = Objects.requireNonNull(accountNumber, "accountNumber");
        this.holder = Objects.requireNonNull(holder, "holder");
        this.type = Objects.requireNonNull(type, "type");
        this.status = Objects.requireNonNull(status, "status");
        this.balance = Objects.requireNonNull(balance, "balance").setScale(2, RoundingMode.HALF_UP);
        this.openedAt = Objects.requireNonNull(openedAt, "openedAt");
        this.closedAt = closedAt;
    }

    /** 신규 개설. id 는 영속화 후 {@link #assignId} 로 주입. */
    public static Account open(AccountNumber accountNumber, String holder, AccountType type, Clock clock) {
        if (holder == null || holder.isBlank()) {
            throw new IllegalArgumentException("예금주명은 비어 있을 수 없습니다");
        }
        Objects.requireNonNull(clock, "clock");
        return new Account(
                null,
                accountNumber,
                holder.trim(),
                type,
                AccountStatus.ACTIVE,
                BigDecimal.ZERO,
                clock.instant(),
                null
        );
    }

    /** 저장소(JPA)에서 재구성. infra 레이어 전용 진입점. */
    public static Account reconstitute(Long id, AccountNumber accountNumber, String holder,
                                       AccountType type, AccountStatus status, BigDecimal balance,
                                       Instant openedAt, Instant closedAt) {
        return new Account(id, accountNumber, holder, type, status, balance, openedAt, closedAt);
    }

    /**
     * 해지 처리.
     * 이미 CLOSED 이면 {@link AccountAlreadyClosedException},
     * 잔액이 0 보다 크면 {@link AccountNotEmptyException}.
     */
    public void close(Clock clock) {
        Objects.requireNonNull(clock, "clock");
        if (status == AccountStatus.CLOSED) {
            throw new AccountAlreadyClosedException(accountNumber);
        }
        if (balance.compareTo(BigDecimal.ZERO) > 0) {
            throw new AccountNotEmptyException(accountNumber, balance);
        }
        this.status = AccountStatus.CLOSED;
        this.closedAt = clock.instant();
    }

    /** 최초 persist 직후 id 를 바인딩하기 위한 제한 API (infra 레이어 Mapper 용도). */
    public void assignId(Long id) {
        if (this.id != null) {
            throw new IllegalStateException("이미 id 가 할당된 계좌입니다: " + this.id);
        }
        this.id = Objects.requireNonNull(id, "id");
    }

    public Long id() { return id; }
    public AccountNumber accountNumber() { return accountNumber; }
    public String holder() { return holder; }
    public AccountType type() { return type; }
    public AccountStatus status() { return status; }
    public BigDecimal balance() { return balance; }
    public Instant openedAt() { return openedAt; }
    public Instant closedAt() { return closedAt; }
}
