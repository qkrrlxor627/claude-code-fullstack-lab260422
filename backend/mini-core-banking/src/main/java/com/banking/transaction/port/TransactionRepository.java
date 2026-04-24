package com.banking.transaction.port;

import com.banking.account.domain.AccountNumber;
import com.banking.transaction.domain.Transaction;

import java.util.List;

/**
 * 거래 이력 저장소 포트 (헥사고날).
 */
public interface TransactionRepository {

    Transaction save(Transaction transaction);

    List<Transaction> findRecentByAccount(AccountNumber account, int limit);

    List<Transaction> findByTransferId(String transferId);
}
