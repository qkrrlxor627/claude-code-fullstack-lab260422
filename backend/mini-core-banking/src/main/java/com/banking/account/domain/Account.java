package com.banking.account.domain;

import com.banking.account.exception.AccountAlreadyClosedException;
import com.banking.account.exception.AccountNotEmptyException;
import com.banking.account.exception.InsufficientBalanceException;
import com.banking.account.exception.InvalidAmountException;

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
 * - 출금은 잔액 검증 필수, 동시성은 서비스 레이어의 비관적 락으로 보장.
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

    /**
     * 입금. 양수 금액을 잔액에 더한다.
     * 금액 유효성 실패 → {@link InvalidAmountException},
     * 이미 해지된 계좌 → {@link AccountAlreadyClosedException}.
     * <p>DORMANT 계좌도 입금은 허용 (휴면 계좌 재활성화는 별도 정책).
     * <p>TODO(확장): 입금 완료 시 Transaction 엔티티 한 건 기록 + DepositCompletedEvent 발행.
     */
    public void deposit(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidAmountException(amount);
        }
        if (status == AccountStatus.CLOSED) {
            throw new AccountAlreadyClosedException(accountNumber);
        }
        this.balance = this.balance.add(amount.setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * 출금. 양수 금액을 잔액에서 차감한다.
     * 금액 유효성 실패 → {@link InvalidAmountException},
     * 해지된 계좌 → {@link AccountAlreadyClosedException},
     * 잔액 부족 → {@link InsufficientBalanceException}.
     * <p>호출자(Service)는 반드시 비관적 락({@code findByNumberForUpdate})과 함께 사용해야 한다.
     */
    public void withdraw(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidAmountException(amount);
        }
        if (status == AccountStatus.CLOSED) {
            throw new AccountAlreadyClosedException(accountNumber);
        }
        BigDecimal scaled = amount.setScale(2, RoundingMode.HALF_UP);
        if (this.balance.compareTo(scaled) < 0) {
            throw new InsufficientBalanceException(accountNumber, scaled, this.balance);
        }
        this.balance = this.balance.subtract(scaled);
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
