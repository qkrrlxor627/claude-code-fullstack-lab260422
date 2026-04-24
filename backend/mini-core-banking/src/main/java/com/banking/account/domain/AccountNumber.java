package com.banking.account.domain;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 계좌번호 값 객체.
 * 형식: {은행코드 3}-{상품코드 2}-{일련번호 7}-{검증번호 1}
 * 예: 110-01-1234567-X
 *
 * 검증번호는 Luhn 알고리즘으로 계산.
 * 이유: 사용자가 한 자리를 잘못 입력하면 즉시 감지 — 타행이체 실수 방지.
 */
public record AccountNumber(String value) {

    private static final Pattern FORMAT = Pattern.compile("^(\\d{3})-(\\d{2})-(\\d{7})-(\\d{1})$");

    public AccountNumber {
        Objects.requireNonNull(value, "계좌번호는 null 일 수 없습니다");
        if (!FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "계좌번호 형식이 올바르지 않습니다 (기대: NNN-NN-NNNNNNN-N): " + value);
        }
        if (!isValidCheckDigit(value)) {
            throw new IllegalArgumentException("계좌번호 검증번호가 올바르지 않습니다: " + value);
        }
    }

    /**
     * 은행코드 + 상품코드 + 일련번호로 유효한 계좌번호를 생성한다.
     * 검증번호는 Luhn 알고리즘으로 자동 계산.
     */
    public static AccountNumber generate(String bankCode, String productCode, long sequence) {
        if (bankCode == null || !bankCode.matches("\\d{3}")) {
            throw new IllegalArgumentException("은행코드는 숫자 3자리여야 합니다: " + bankCode);
        }
        if (productCode == null || !productCode.matches("\\d{2}")) {
            throw new IllegalArgumentException("상품코드는 숫자 2자리여야 합니다: " + productCode);
        }
        if (sequence < 0 || sequence > 9_999_999L) {
            throw new IllegalArgumentException("일련번호는 0~9,999,999 범위여야 합니다: " + sequence);
        }

        String seqStr = String.format("%07d", sequence);
        String base = bankCode + productCode + seqStr; // 12 digits
        int checkDigit = luhnCheckDigit(base);
        String formatted = "%s-%s-%s-%d".formatted(bankCode, productCode, seqStr, checkDigit);
        return new AccountNumber(formatted);
    }

    private static boolean isValidCheckDigit(String formatted) {
        String digits = formatted.replace("-", ""); // 13 digits
        String base = digits.substring(0, 12);
        int expected = luhnCheckDigit(base);
        int actual = digits.charAt(12) - '0';
        return expected == actual;
    }

    /**
     * Luhn 알고리즘: 오른쪽(검증번호가 들어갈 자리 기준)으로부터 거리가 홀수인 자리를 2배,
     * 결과가 10 이상이면 9 를 빼고 합산. (sum + check) % 10 == 0 이 되도록 check 계산.
     */
    private static int luhnCheckDigit(String base) {
        int sum = 0;
        for (int i = base.length() - 1; i >= 0; i--) {
            int digit = base.charAt(i) - '0';
            int distanceFromCheck = base.length() - i; // 1-indexed
            int effective = (distanceFromCheck % 2 == 1) ? digit * 2 : digit;
            if (effective > 9) {
                effective -= 9;
            }
            sum += effective;
        }
        return (10 - (sum % 10)) % 10;
    }
}
