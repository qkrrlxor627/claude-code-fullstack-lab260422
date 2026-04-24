package com.banking.transfer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "계좌 이체 요청")
public record TransferRequest(
        @Schema(description = "출금 계좌번호", example = "110-01-1000000-X", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String fromAccount,

        @Schema(description = "입금 계좌번호", example = "110-01-1000001-2", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String toAccount,

        @Schema(description = "이체 금액 (원)", example = "3000.00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Pattern(regexp = "^[0-9]+(\\.[0-9]{1,2})?$",
                message = "금액은 숫자, 선택적으로 소수점 둘째자리까지 허용됩니다")
        String amount
) {
}
