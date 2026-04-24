package com.banking.transfer;

import com.banking.common.response.ApiResponse;
import com.banking.transfer.dto.TransferRequest;
import com.banking.transfer.dto.TransferResponse;
import com.banking.account.domain.AccountNumber;
import com.banking.transfer.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/transfers")
@Tag(name = "Transfer", description = "계좌 이체 (멱등성 키 필수)")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    @Operation(summary = "계좌 이체",
            description = "X-Idempotency-Key 헤더 필수. 같은 키로 재전송 시 최초 응답을 그대로 반환.")
    public ApiResponse<TransferResponse> transfer(
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransferRequest request) {
        TransferResponse result = transferService.transfer(
                new AccountNumber(request.fromAccount()),
                new AccountNumber(request.toAccount()),
                new BigDecimal(request.amount()),
                idempotencyKey);
        return ApiResponse.success(result);
    }
}
