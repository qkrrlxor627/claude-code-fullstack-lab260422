package com.banking.domain.account;

import java.util.Optional;

/**
 * 계좌 저장소 포트 (헥사고날).
 * infra 레이어의 AccountRepositoryAdapter 가 구현한다.
 * domain 은 JPA/DB 에 대해 무지해야 한다.
 */
public interface AccountRepository {

    Account save(Account account);

    Optional<Account> findByNumber(AccountNumber accountNumber);

    boolean existsByNumber(AccountNumber accountNumber);

    /**
     * 계좌번호 일련번호 채번.
     * infra 구현체는 DB 시퀀스(account_number_seq)로부터 유일값을 발급한다.
     */
    long nextSequence();
}
