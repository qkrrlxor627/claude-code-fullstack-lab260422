package com.banking.account.infra;

import com.banking.account.domain.Account;
import com.banking.account.domain.AccountNumber;

/**
 * 도메인 Account ↔ JPA AccountJpaEntity 변환.
 * static 메서드만 사용 — 무상태.
 */
final class AccountMapper {

    private AccountMapper() {
    }

    static AccountJpaEntity toJpa(Account account) {
        return new AccountJpaEntity(
                account.id(),
                account.accountNumber().value(),
                account.holder(),
                account.type(),
                account.status(),
                account.balance(),
                account.openedAt(),
                account.closedAt()
        );
    }

    static Account toDomain(AccountJpaEntity entity) {
        return Account.reconstitute(
                entity.getId(),
                new AccountNumber(entity.getAccountNumber()),
                entity.getHolder(),
                entity.getType(),
                entity.getStatus(),
                entity.getBalance(),
                entity.getOpenedAt(),
                entity.getClosedAt()
        );
    }
}
