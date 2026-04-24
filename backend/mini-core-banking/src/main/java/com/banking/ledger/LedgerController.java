package com.banking.ledger;

import com.banking.common.response.ApiResponse;
import com.banking.ledger.dto.LedgerEntryResponse;
import com.banking.ledger.port.LedgerEntryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 원장 조회 (관리자/감사 용). 일반 사용자 UI 에는 노출하지 않음.
 */
@RestController
@RequestMapping("/api/v1/ledger")
@Tag(name = "Ledger", description = "원장 (복식부기) — 관리자 조회")
public class LedgerController {

    private final LedgerEntryRepository ledgerRepository;

    public LedgerController(LedgerEntryRepository ledgerRepository) {
        this.ledgerRepository = ledgerRepository;
    }

    @GetMapping
    @Operation(summary = "계좌 원장 조회", description = "account_code 기준 최신순. accountCode 는 실계좌번호 또는 CASH_ASSET.")
    public ApiResponse<List<LedgerEntryResponse>> byAccount(
            @RequestParam String accountCode,
            @RequestParam(defaultValue = "50") int limit) {
        int clamped = Math.min(Math.max(limit, 1), 500);
        List<LedgerEntryResponse> items = ledgerRepository.findByAccountCode(accountCode, clamped)
                .stream().map(LedgerEntryResponse::from).toList();
        return ApiResponse.success(items);
    }

    @GetMapping("/transaction/{transactionId}")
    @Operation(summary = "거래 단위 원장 조회",
            description = "한 거래의 차변/대변 모두 반환. 차변 합 == 대변 합 검증 가능.")
    public ApiResponse<List<LedgerEntryResponse>> byTransaction(@PathVariable Long transactionId) {
        List<LedgerEntryResponse> items = ledgerRepository.findByTransactionId(transactionId)
                .stream().map(LedgerEntryResponse::from).toList();
        return ApiResponse.success(items);
    }
}
