package com.banking.domain.account.exception;

import com.banking.common.exception.BankingException;
import com.banking.domain.account.AccountNumber;

public class AccountNotFoundException extends BankingException {

    public AccountNotFoundException(AccountNumber accountNumber) {
        super("ACCOUNT_NOT_FOUND",
                "계좌를 찾을 수 없습니다: " + (accountNumber != null ? accountNumber.value() : "null"));
    }
}
