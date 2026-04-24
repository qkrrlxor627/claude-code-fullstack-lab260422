package com.banking.account.exception;

import com.banking.common.exception.BankingException;

import java.math.BigDecimal;

/**
 * 입출금/이체 금액이 유효하지 않을 때 발생 (null / 0 / 음수).
 */
public class InvalidAmountException extends BankingException {

    public InvalidAmountException(BigDecimal amount) {
        super("INVALID_AMOUNT",
                "금액은 0보다 커야 합니다 (요청: %s)"
                        .formatted(amount == null ? "null" : amount.toPlainString()));
    }
}
