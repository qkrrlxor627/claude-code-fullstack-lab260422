package com.banking.transaction.infra;

import com.banking.transaction.infra.TransactionJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, Long> {

    List<TransactionJpaEntity> findByAccountNumberOrderByCreatedAtDesc(String accountNumber, Pageable pageable);

    List<TransactionJpaEntity> findByTransferId(String transferId);
}
