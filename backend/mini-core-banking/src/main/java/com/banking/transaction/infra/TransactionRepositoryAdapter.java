package com.banking.transaction.infra;

import com.banking.account.domain.AccountNumber;
import com.banking.transaction.domain.Transaction;
import com.banking.transaction.port.TransactionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TransactionRepositoryAdapter implements TransactionRepository {

    private final TransactionJpaRepository jpa;

    public TransactionRepositoryAdapter(TransactionJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionJpaEntity entity = new TransactionJpaEntity(
                transaction.id(),
                transaction.accountNumber().value(),
                transaction.type(),
                transaction.amount(),
                transaction.counterparty() != null ? transaction.counterparty().value() : null,
                transaction.transferId(),
                transaction.balanceAfter(),
                transaction.createdAt()
        );
        TransactionJpaEntity saved = jpa.save(entity);
        if (transaction.id() == null) {
            return transaction.withId(saved.getId());
        }
        return transaction;
    }

    @Override
    public List<Transaction> findRecentByAccount(AccountNumber account, int limit) {
        return jpa
                .findByAccountNumberOrderByCreatedAtDesc(account.value(), PageRequest.of(0, limit))
                .stream()
                .map(TransactionRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    public List<Transaction> findByTransferId(String transferId) {
        return jpa.findByTransferId(transferId).stream()
                .map(TransactionRepositoryAdapter::toDomain)
                .toList();
    }

    private static Transaction toDomain(TransactionJpaEntity e) {
        return Transaction.reconstitute(
                e.getId(),
                new AccountNumber(e.getAccountNumber()),
                e.getType(),
                e.getAmount(),
                e.getCounterpartyAccount() != null ? new AccountNumber(e.getCounterpartyAccount()) : null,
                e.getTransferId(),
                e.getBalanceAfter(),
                e.getCreatedAt()
        );
    }
}
