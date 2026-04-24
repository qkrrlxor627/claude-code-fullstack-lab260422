package com.banking.account.exception;

import com.banking.account.domain.AccountNumber;
import com.banking.common.exception.BankingException;

import java.math.BigDecimal;

/**
 * 출금/이체 시 잔액이 요청 금액보다 작을 때 발생.
 */
public class InsufficientBalanceException extends BankingException {

    public InsufficientBalanceException(AccountNumber accountNumber,
                                        BigDecimal requested,
                                        BigDecimal available) {
        super("INSUFFICIENT_BALANCE",
                "잔액이 부족합니다 (계좌: %s, 요청: %s, 잔액: %s)".formatted(
                        accountNumber.value(),
                        requested.toPlainString(),
                        available.toPlainString()));
    }
}
