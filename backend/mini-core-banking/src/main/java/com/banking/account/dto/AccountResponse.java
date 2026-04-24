package com.banking.account.dto;

import com.banking.account.domain.Account;
import com.banking.account.domain.AccountStatus;
import com.banking.account.domain.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "계좌 응답")
public record AccountResponse(
        @Schema(description = "계좌번호", example = "110-01-1000000-X")
        String accountNumber,

        @Schema(description = "예금주명", example = "김싸피")
        String holder,

        @Schema(description = "상품 유형")
        AccountType type,

        @Schema(description = "계좌 상태")
        AccountStatus status,

        @Schema(description = "잔액 (원). 문자열로 전달하여 JSON 부동소수점 오차를 원천 차단.",
                example = "0.00")
        String balance,

        @Schema(description = "개설 시각 (UTC Instant)")
        Instant openedAt,

        @Schema(description = "해지 시각 (해지 전에는 null)")
        Instant closedAt
) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.accountNumber().value(),
                account.holder(),
                account.type(),
                account.status(),
                account.balance().toPlainString(),
                account.openedAt(),
                account.closedAt()
        );
    }
}
