package com.banking.transfer.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TransferResponse(
        String transferId,
        String fromAccount,
        String toAccount,
        BigDecimal amount,
        BigDecimal fromBalanceAfter,
        BigDecimal toBalanceAfter,
        Instant executedAt
) {
}
