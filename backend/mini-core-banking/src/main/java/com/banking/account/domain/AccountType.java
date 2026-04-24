package com.banking.account.domain;

/**
 * 계좌 상품 유형.
 * 상품코드(2자리) 매핑은 AccountService 에서 수행.
 */
public enum AccountType {
    /** 보통예금 */
    SAVINGS,
    /** 정기예금 */
    FIXED_DEPOSIT,
    /** 대출계좌 */
    LOAN
}
