package com.banking.ledger.dto;

import com.banking.ledger.domain.LedgerEntry;
import com.banking.ledger.domain.LedgerSide;

import java.math.BigDecimal;
import java.time.Instant;

public record LedgerEntryResponse(
        Long id,
        Long transactionId,
        String accountCode,
        LedgerSide side,
        BigDecimal amount,
        String memo,
        Instant createdAt
) {
    public static LedgerEntryResponse from(LedgerEntry e) {
        return new LedgerEntryResponse(
                e.id(), e.transactionId(), e.accountCode(),
                e.side(), e.amount(), e.memo(), e.createdAt());
    }
}
