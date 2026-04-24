package com.banking.account.dto;

import com.banking.account.domain.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "계좌 개설 요청")
public record OpenAccountRequest(
        @Schema(description = "예금주명", example = "김싸피", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(min = 2, max = 50)
        String holder,

        @Schema(description = "상품 유형", example = "SAVINGS", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        AccountType type
) {
}
