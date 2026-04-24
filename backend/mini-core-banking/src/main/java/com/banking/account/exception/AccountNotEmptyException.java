package com.banking.account.exception;

import com.banking.common.exception.BankingException;
import com.banking.account.domain.AccountNumber;

import java.math.BigDecimal;

/**
 * 해지 시도 시 잔액이 0원이 아닐 때 발생.
 * 은행 실무에서는 예수부채 (고객 자금) 가 남아있는 계좌를 함부로 폐기할 수 없다.
 */
public class AccountNotEmptyException extends BankingException {

    public AccountNotEmptyException(AccountNumber accountNumber, BigDecimal balance) {
        super("ACCOUNT_NOT_EMPTY",
                "잔액이 남아있어 해지할 수 없습니다 (계좌: %s, 잔액: %s 원)"
                        .formatted(accountNumber.value(), balance.toPlainString()));
    }
}
