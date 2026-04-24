package com.banking.account.domain;

import com.banking.account.exception.AccountAlreadyClosedException;
import com.banking.account.exception.AccountNotEmptyException;
import com.banking.account.exception.InsufficientBalanceException;
import com.banking.account.exception.InvalidAmountException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

    private static final AccountNumber NUMBER = AccountNumber.generate("110", "01", 1234567L);
    private static final Clock FIXED = Clock.fixed(Instant.parse("2026-04-22T00:00:00Z"), ZoneId.of("UTC"));

    @Test
    @DisplayName("open — 신규 계좌는 ACTIVE 상태, 잔액 0, openedAt 기록")
    void open_정상() {
        Account account = Account.open(NUMBER, "김싸피", AccountType.SAVINGS, FIXED);

        assertThat(account.accountNumber()).isEqualTo(NUMBER);
        assertThat(account.holder()).isEqualTo("김싸피");
        assertThat(account.type()).isEqualTo(AccountType.SAVINGS);
        assertThat(account.status()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.balance()).isEqualByComparingTo("0.00");
        assertThat(account.openedAt()).isEqualTo(Instant.parse("2026-04-22T00:00:00Z"));
        assertThat(account.closedAt()).isNull();
        assertThat(account.id()).isNull();
    }

    @Test
    @DisplayName("open — 예금주명이 공백이면 예외")
    void open_예금주명_공백() {
        assertThatThrownBy(() -> Account.open(NUMBER, "   ", AccountType.SAVINGS, FIXED))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Account.open(NUMBER, null, AccountType.SAVINGS, FIXED))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("close — 잔액 0 이면 정상 해지, 상태 CLOSED, closedAt 기록")
    void close_정상() {
        Account account = Account.open(NUMBER, "김싸피", AccountType.SAVINGS, FIXED);
        Clock later = Clock.fixed(Instant.parse("2026-05-01T00:00:00Z"), ZoneId.of("UTC"));

        account.close(later);

        assertThat(account.status()).isEqualTo(AccountStatus.CLOSED);
        assertThat(account.closedAt()).isEqualTo(Instant.parse("2026-05-01T00:00:00Z"));
    }

    @Test
    @DisplayName("close — 잔액이 0 보다 크면 AccountNotEmptyException")
    void close_잔액_남음() {
        Account account = Account.reconstitute(
                1L, NUMBER, "김싸피", AccountType.SAVINGS,
                AccountStatus.ACTIVE, new BigDecimal("100.00"),
                Instant.parse("2026-04-01T00:00:00Z"), null);

        assertThatThrownBy(() -> account.close(FIXED))
                .isInstanceOf(AccountNotEmptyException.class)
                .hasMessageContaining("100.00");

        assertThat(account.status()).isEqualTo(AccountStatus.ACTIVE); // 롤백 상태
    }

    @Test
    @DisplayName("close — 이미 CLOSED 인 계좌는 AccountAlreadyClosedException")
    void close_이미_해지() {
        Account account = Account.reconstitute(
                1L, NUMBER, "김싸피", AccountType.SAVINGS,
                AccountStatus.CLOSED, BigDecimal.ZERO,
                Instant.parse("2026-04-01T00:00:00Z"),
                Instant.parse("2026-04-10T00:00:00Z"));

        assertThatThrownBy(() -> account.close(FIXED))
                .isInstanceOf(AccountAlreadyClosedException.class);
    }

    @Test
    @DisplayName("deposit — 정상 입금은 잔액 증가 (scale=2 유지)")
    void deposit_정상() {
        Account account = Account.open(NUMBER, "김싸피", AccountType.SAVINGS, FIXED);

        account.deposit(new BigDecimal("10000"));
        assertThat(account.balance()).isEqualByComparingTo("10000.00");

        account.deposit(new BigDecimal("500.50"));
        assertThat(account.balance()).isEqualByComparingTo("10500.50");
    }

    @Test
    @DisplayName("deposit — null / 0 / 음수 금액은 InvalidAmountException")
    void deposit_금액_유효성() {
        Account account = Account.open(NUMBER, "김싸피", AccountType.SAVINGS, FIXED);

        assertThatThrownBy(() -> account.deposit(null))
                .isInstanceOf(InvalidAmountException.class);
        assertThatThrownBy(() -> account.deposit(BigDecimal.ZERO))
                .isInstanceOf(InvalidAmountException.class);
        assertThatThrownBy(() -> account.deposit(new BigDecimal("-100")))
                .isInstanceOf(InvalidAmountException.class);

        assertThat(account.balance()).isEqualByComparingTo("0.00"); // 잔액 변동 없음
    }

    @Test
    @DisplayName("deposit — 해지된 계좌는 AccountAlreadyClosedException")
    void deposit_해지된_계좌() {
        Account closed = Account.reconstitute(
                1L, NUMBER, "김싸피", AccountType.SAVINGS,
                AccountStatus.CLOSED, BigDecimal.ZERO,
                Instant.parse("2026-04-01T00:00:00Z"),
                Instant.parse("2026-04-10T00:00:00Z"));

        assertThatThrownBy(() -> closed.deposit(new BigDecimal("1000")))
                .isInstanceOf(AccountAlreadyClosedException.class);
    }

    @Test
    @DisplayName("withdraw — 정상 출금은 잔액 감소 (scale=2 유지)")
    void withdraw_정상() {
        Account account = Account.reconstitute(
                1L, NUMBER, "김싸피", AccountType.SAVINGS,
                AccountStatus.ACTIVE, new BigDecimal("10000.00"),
                Instant.parse("2026-04-01T00:00:00Z"), null);

        account.withdraw(new BigDecimal("3000"));
        assertThat(account.balance()).isEqualByComparingTo("7000.00");

        account.withdraw(new BigDecimal("500.50"));
        assertThat(account.balance()).isEqualByComparingTo("6499.50");
    }

    @Test
    @DisplayName("withdraw — null / 0 / 음수 금액은 InvalidAmountException")
    void withdraw_금액_유효성() {
        Account account = Account.reconstitute(
                1L, NUMBER, "김싸피", AccountType.SAVINGS,
                AccountStatus.ACTIVE, new BigDecimal("1000.00"),
                Instant.parse("2026-04-01T00:00:00Z"), null);

        assertThatThrownBy(() -> account.withdraw(null))
                .isInstanceOf(InvalidAmountException.class);
        assertThatThrownBy(() -> account.withdraw(BigDecimal.ZERO))
                .isInstanceOf(InvalidAmountException.class);
        assertThatThrownBy(() -> account.withdraw(new BigDecimal("-100")))
                .isInstanceOf(InvalidAmountException.class);

        assertThat(account.balance()).isEqualByComparingTo("1000.00"); // 잔액 변동 없음
    }

    @Test
    @DisplayName("withdraw — 잔액보다 큰 금액은 InsufficientBalanceException")
    void withdraw_잔액_부족() {
        Account account = Account.reconstitute(
                1L, NUMBER, "김싸피", AccountType.SAVINGS,
                AccountStatus.ACTIVE, new BigDecimal("100.00"),
                Instant.parse("2026-04-01T00:00:00Z"), null);

        assertThatThrownBy(() -> account.withdraw(new BigDecimal("100.01")))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("100.01")
                .hasMessageContaining("100.00");

        assertThat(account.balance()).isEqualByComparingTo("100.00"); // 잔액 변동 없음
    }

    @Test
    @DisplayName("withdraw — 해지된 계좌는 AccountAlreadyClosedException")
    void withdraw_해지된_계좌() {
        Account closed = Account.reconstitute(
                1L, NUMBER, "김싸피", AccountType.SAVINGS,
                AccountStatus.CLOSED, BigDecimal.ZERO,
                Instant.parse("2026-04-01T00:00:00Z"),
                Instant.parse("2026-04-10T00:00:00Z"));

        assertThatThrownBy(() -> closed.withdraw(new BigDecimal("1000")))
                .isInstanceOf(AccountAlreadyClosedException.class);
    }

    @Test
    @DisplayName("assignId — 한 번만 가능, 두 번째는 예외")
    void assignId_한번만() {
        Account account = Account.open(NUMBER, "김싸피", AccountType.SAVINGS, FIXED);
        account.assignId(42L);

        assertThat(account.id()).isEqualTo(42L);
        assertThatThrownBy(() -> account.assignId(43L))
                .isInstanceOf(IllegalStateException.class);
    }
}
