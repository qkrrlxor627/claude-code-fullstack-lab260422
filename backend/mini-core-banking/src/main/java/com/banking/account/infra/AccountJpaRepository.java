package com.banking.account.infra;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Spring Data JPA 인터페이스. infra 내부에서만 사용.
 */
interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, Long> {

    Optional<AccountJpaEntity> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    /**
     * 비관적 쓰기 락 (PostgreSQL: SELECT ... FOR UPDATE).
     * 출금/이체 등 잔액 변경 트랜잭션에서만 사용.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AccountJpaEntity a where a.accountNumber = :accountNumber")
    Optional<AccountJpaEntity> findByAccountNumberForUpdate(@Param("accountNumber") String accountNumber);
}
