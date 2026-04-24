package com.banking.ledger.infra;

import com.banking.ledger.infra.LedgerEntryJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface LedgerEntryJpaRepository extends JpaRepository<LedgerEntryJpaEntity, Long> {

    List<LedgerEntryJpaEntity> findByAccountCodeOrderByCreatedAtDesc(String accountCode, Pageable pageable);

    List<LedgerEntryJpaEntity> findByTransactionIdOrderByIdAsc(Long transactionId);
}
