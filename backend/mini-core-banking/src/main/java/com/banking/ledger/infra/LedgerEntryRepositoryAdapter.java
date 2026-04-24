package com.banking.ledger.infra;

import com.banking.ledger.domain.LedgerEntry;
import com.banking.ledger.port.LedgerEntryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class LedgerEntryRepositoryAdapter implements LedgerEntryRepository {

    private final LedgerEntryJpaRepository jpa;

    public LedgerEntryRepositoryAdapter(LedgerEntryJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public LedgerEntry save(LedgerEntry entry) {
        LedgerEntryJpaEntity entity = new LedgerEntryJpaEntity(
                entry.id(),
                entry.transactionId(),
                entry.accountCode(),
                entry.side(),
                entry.amount(),
                entry.memo(),
                entry.createdAt()
        );
        LedgerEntryJpaEntity saved = jpa.save(entity);
        if (entry.id() == null) {
            return entry.withId(saved.getId());
        }
        return entry;
    }

    @Override
    public List<LedgerEntry> findByAccountCode(String accountCode, int limit) {
        return jpa
                .findByAccountCodeOrderByCreatedAtDesc(accountCode, PageRequest.of(0, limit))
                .stream()
                .map(LedgerEntryRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    public List<LedgerEntry> findByTransactionId(Long transactionId) {
        return jpa.findByTransactionIdOrderByIdAsc(transactionId).stream()
                .map(LedgerEntryRepositoryAdapter::toDomain)
                .toList();
    }

    private static LedgerEntry toDomain(LedgerEntryJpaEntity e) {
        return LedgerEntry.reconstitute(
                e.getId(),
                e.getTransactionId(),
                e.getAccountCode(),
                e.getSide(),
                e.getAmount(),
                e.getMemo(),
                e.getCreatedAt()
        );
    }
}
