package com.banking.account.port;

import com.banking.account.domain.Account;
import com.banking.account.domain.AccountNumber;

import java.util.Optional;

/**
 * 계좌 저장소 포트 (헥사고날).
 * infra 레이어의 AccountRepositoryAdapter 가 구현한다.
 * domain 은 JPA/DB 에 대해 무지해야 한다.
 */
public interface AccountRepository {

    Account save(Account account);

    Optional<Account> findByNumber(AccountNumber accountNumber);

    /**
     * 비관적 락(SELECT FOR UPDATE) 으로 계좌 조회.
     * 출금/이체처럼 잔액을 변경하는 트랜잭션에서 사용.
     * 호출 트랜잭션이 끝날 때까지 다른 트랜잭션은 같은 row 를 읽을 수 없다.
     */
    Optional<Account> findByNumberForUpdate(AccountNumber accountNumber);

    boolean existsByNumber(AccountNumber accountNumber);

    /**
     * 계좌번호 일련번호 채번.
     * infra 구현체는 DB 시퀀스(account_number_seq)로부터 유일값을 발급한다.
     */
    long nextSequence();
}
