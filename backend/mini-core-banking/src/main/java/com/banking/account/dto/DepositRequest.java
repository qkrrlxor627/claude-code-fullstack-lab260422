package com.banking.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "입금 요청")
public record DepositRequest(
        @Schema(description = "입금 금액 (원). 문자열로 전달해야 부동소수점 오차가 발생하지 않는다.",
                example = "10000.00",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Pattern(regexp = "^[0-9]+(\\.[0-9]{1,2})?$",
                message = "금액은 숫자, 선택적으로 소수점 둘째자리까지 허용됩니다")
        String amount
) {
}
