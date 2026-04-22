package com.banking.common.exception;

/**
 * 은행 도메인 예외 최상위 클래스.
 * 모든 도메인 예외는 이 클래스를 상속한다.
 * 이유: 글로벌 핸들러에서 일관된 에러 응답을 만들기 위해.
 */
public abstract class BankingException extends RuntimeException {

    private final String code;

    protected BankingException(String code, String message) {
        super(message);
        this.code = code;
    }

    protected BankingException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
