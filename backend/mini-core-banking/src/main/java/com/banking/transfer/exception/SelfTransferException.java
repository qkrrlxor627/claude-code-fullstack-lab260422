package com.banking.transfer.exception;

import com.banking.account.domain.AccountNumber;
import com.banking.common.exception.BankingException;

public class SelfTransferException extends BankingException {

    public SelfTransferException(AccountNumber account) {
        super("SELF_TRANSFER",
                "자기 자신에게 이체할 수 없습니다 (계좌: %s)".formatted(account.value()));
    }
}
