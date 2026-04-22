package com.banking.domain.account.exception;

import com.banking.common.exception.BankingException;
import com.banking.domain.account.AccountNumber;

public class AccountAlreadyClosedException extends BankingException {

    public AccountAlreadyClosedException(AccountNumber accountNumber) {
        super("ACCOUNT_ALREADY_CLOSED",
                "이미 해지된 계좌입니다: " + accountNumber.value());
    }
}
