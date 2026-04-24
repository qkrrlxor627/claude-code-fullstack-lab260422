package com.banking.transaction.dto;

import com.banking.transaction.domain.Transaction;
import com.banking.transaction.domain.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        Long id,
        String accountNumber,
        TransactionType type,
        BigDecimal amount,
        String counterpartyAccount,
        String transferId,
        BigDecimal balanceAfter,
        Instant createdAt
) {
    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(
                t.id(),
                t.accountNumber().value(),
                t.type(),
                t.amount(),
                t.counterparty() != null ? t.counterparty().value() : null,
                t.transferId(),
                t.balanceAfter(),
                t.createdAt()
        );
    }
}
