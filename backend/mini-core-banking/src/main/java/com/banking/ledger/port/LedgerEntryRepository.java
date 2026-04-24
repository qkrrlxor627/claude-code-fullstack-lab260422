package com.banking.ledger.port;

import com.banking.ledger.domain.LedgerEntry;

import java.util.List;

public interface LedgerEntryRepository {

    /** 수정/삭제는 제공하지 않음 — 원장 불변 원칙. */
    LedgerEntry save(LedgerEntry entry);

    List<LedgerEntry> findByAccountCode(String accountCode, int limit);

    List<LedgerEntry> findByTransactionId(Long transactionId);
}
