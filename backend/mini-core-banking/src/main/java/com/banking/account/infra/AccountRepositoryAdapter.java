package com.banking.account.infra;

import com.banking.account.domain.Account;
import com.banking.account.domain.AccountNumber;
import com.banking.account.port.AccountRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * domain.AccountRepository 포트의 JPA/PostgreSQL 어댑터.
 *
 * 설계 주의:
 * - 도메인 Account 와 JPA 엔티티를 Mapper 로 분리.
 * - save 후 JPA 가 생성한 id 는 Account.assignId 로 도메인에 역주입.
 * - nextSequence 는 네이티브 쿼리로 Postgres 시퀀스에서 직접 pull (JPA @SequenceGenerator 대신).
 */
@Repository
public class AccountRepositoryAdapter implements AccountRepository {

    private final AccountJpaRepository jpa;

    @PersistenceContext
    private EntityManager entityManager;

    public AccountRepositoryAdapter(AccountJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Account save(Account account) {
        AccountJpaEntity entity = AccountMapper.toJpa(account);
        AccountJpaEntity saved = jpa.save(entity);
        if (account.id() == null && saved.getId() != null) {
            account.assignId(saved.getId());
        }
        return account;
    }

    @Override
    public Optional<Account> findByNumber(AccountNumber accountNumber) {
        return jpa.findByAccountNumber(accountNumber.value())
                .map(AccountMapper::toDomain);
    }

    @Override
    public Optional<Account> findByNumberForUpdate(AccountNumber accountNumber) {
        return jpa.findByAccountNumberForUpdate(accountNumber.value())
                .map(AccountMapper::toDomain);
    }

    @Override
    public boolean existsByNumber(AccountNumber accountNumber) {
        return jpa.existsByAccountNumber(accountNumber.value());
    }

    @Override
    public long nextSequence() {
        Object result = entityManager
                .createNativeQuery("SELECT nextval('account_number_seq')")
                .getSingleResult();
        return ((Number) result).longValue();
    }
}
