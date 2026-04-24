package com.banking.transaction.event;

import com.banking.transaction.domain.TransactionType;

import java.math.BigDecimal;

/**
 * 거래가 트랜잭션 레코드까지 저장된 직후 발행되는 이벤트.
 * LedgerEventHandler 가 AFTER_COMMIT 시점에 비동기로 원장에 분개.
 *
 * <p>counterpartyAccount 는 이체일 때만 non-null.
 */
public record TransactionCompletedEvent(
        Long transactionId,
        String accountNumber,
        TransactionType type,
        BigDecimal amount,
        String counterpartyAccount,
        String transferId
) {
}
