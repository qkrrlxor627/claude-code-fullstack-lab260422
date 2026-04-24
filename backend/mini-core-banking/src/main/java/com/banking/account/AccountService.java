package com.banking.account;

import com.banking.account.domain.Account;
import com.banking.account.domain.AccountNumber;
import com.banking.account.domain.AccountType;
import com.banking.transaction.domain.Transaction;
import com.banking.transaction.domain.TransactionType;
import com.banking.transaction.event.TransactionCompletedEvent;
import com.banking.account.exception.AccountNotFoundException;
import com.banking.account.port.AccountRepository;
import com.banking.transaction.port.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;

/**
 * 계좌 도메인 서비스.
 * 개설 / 해지 / 조회 의 orchestration 만 담당. 트랜잭션 경계도 여기서.
 */
@Service
@Transactional
public class AccountService {

    private final AccountRepository repository;
    private final TransactionRepository transactionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;
    private final String bankCode;

    public AccountService(
            AccountRepository repository,
            TransactionRepository transactionRepository,
            ApplicationEventPublisher eventPublisher,
            Clock clock,
            @Value("${banking.account.bank-code:110}") String bankCode
    ) {
        this.repository = repository;
        this.transactionRepository = transactionRepository;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
        this.bankCode = bankCode;
    }

    public Account open(String holder, AccountType type) {
        long sequence = repository.nextSequence();
        AccountNumber number = AccountNumber.generate(bankCode, productCodeFor(type), sequence);
        Account account = Account.open(number, holder, type, clock);
        return repository.save(account);
    }

    @Transactional(readOnly = true)
    public Account find(AccountNumber number) {
        return repository.findByNumber(number)
                .orElseThrow(() -> new AccountNotFoundException(number));
    }

    public Account close(AccountNumber number) {
        Account account = repository.findByNumber(number)
                .orElseThrow(() -> new AccountNotFoundException(number));
        account.close(clock);
        return repository.save(account);
    }

    /**
     * 입금. 비관적 락으로 조회 → 도메인 메서드 위임 → 저장 + Transaction 기록.
     */
    public Account deposit(AccountNumber number, BigDecimal amount) {
        Account account = repository.findByNumberForUpdate(number)
                .orElseThrow(() -> new AccountNotFoundException(number));
        account.deposit(amount);
        Account saved = repository.save(account);
        Transaction tx = transactionRepository.save(Transaction.singleParty(
                saved.accountNumber(), TransactionType.DEPOSIT, amount, saved.balance(), clock));
        eventPublisher.publishEvent(new TransactionCompletedEvent(
                tx.id(), tx.accountNumber().value(), tx.type(), tx.amount(), null, null));
        return saved;
    }

    /**
     * 출금. 비관적 락으로 잔액 검증 + 차감 + Transaction 기록.
     * 잔액 부족 시 {@link com.banking.account.exception.InsufficientBalanceException}.
     */
    public Account withdraw(AccountNumber number, BigDecimal amount) {
        Account account = repository.findByNumberForUpdate(number)
                .orElseThrow(() -> new AccountNotFoundException(number));
        account.withdraw(amount);
        Account saved = repository.save(account);
        Transaction tx = transactionRepository.save(Transaction.singleParty(
                saved.accountNumber(), TransactionType.WITHDRAWAL, amount, saved.balance(), clock));
        eventPublisher.publishEvent(new TransactionCompletedEvent(
                tx.id(), tx.accountNumber().value(), tx.type(), tx.amount(), null, null));
        return saved;
    }

    /** 계좌 최근 거래 내역 조회. */
    @Transactional(readOnly = true)
    public List<Transaction> recentTransactions(AccountNumber number, int limit) {
        if (!repository.existsByNumber(number)) {
            throw new AccountNotFoundException(number);
        }
        return transactionRepository.findRecentByAccount(number, limit);
    }

    private String productCodeFor(AccountType type) {
        return switch (type) {
            case SAVINGS -> "01";
            case FIXED_DEPOSIT -> "02";
            case LOAN -> "10";
        };
    }
}
