package com.banking.domain.account;

import com.banking.domain.account.exception.AccountNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

/**
 * 계좌 도메인 서비스.
 * 개설 / 해지 / 조회 의 orchestration 만 담당. 트랜잭션 경계도 여기서.
 */
@Service
@Transactional
public class AccountService {

    private final AccountRepository repository;
    private final Clock clock;
    private final String bankCode;

    public AccountService(
            AccountRepository repository,
            Clock clock,
            @Value("${banking.account.bank-code:110}") String bankCode
    ) {
        this.repository = repository;
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

    private String productCodeFor(AccountType type) {
        return switch (type) {
            case SAVINGS -> "01";
            case FIXED_DEPOSIT -> "02";
            case LOAN -> "10";
        };
    }
}
