package com.banking.account;

import com.banking.account.dto.AccountResponse;
import com.banking.common.response.ApiResponse;
import com.banking.account.dto.DepositRequest;
import com.banking.account.dto.OpenAccountRequest;
import com.banking.transaction.dto.TransactionResponse;
import com.banking.account.dto.WithdrawRequest;
import com.banking.account.domain.Account;
import com.banking.account.domain.AccountNumber;
import com.banking.account.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Account", description = "계좌 개설/조회/해지")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "계좌 개설", description = "신규 계좌를 개설하고 생성된 계좌번호를 응답한다.")
    public ApiResponse<AccountResponse> open(@Valid @RequestBody OpenAccountRequest request) {
        Account account = accountService.open(request.holder(), request.type());
        return ApiResponse.success(AccountResponse.from(account));
    }

    @GetMapping("/{accountNumber}")
    @Operation(summary = "계좌 조회", description = "계좌번호로 단건 조회.")
    public ApiResponse<AccountResponse> find(
            @Parameter(description = "계좌번호 (형식: NNN-NN-NNNNNNN-N)", example = "110-01-1000000-X")
            @PathVariable String accountNumber) {
        Account account = accountService.find(new AccountNumber(accountNumber));
        return ApiResponse.success(AccountResponse.from(account));
    }

    @DeleteMapping("/{accountNumber}")
    @Operation(summary = "계좌 해지",
            description = "계좌 상태를 CLOSED 로 전환한다. 잔액이 0 보다 크면 실패.")
    public ApiResponse<AccountResponse> close(
            @Parameter(description = "계좌번호", example = "110-01-1000000-X")
            @PathVariable String accountNumber) {
        Account account = accountService.close(new AccountNumber(accountNumber));
        return ApiResponse.success(AccountResponse.from(account));
    }

    @PostMapping("/{accountNumber}/deposits")
    @Operation(summary = "입금",
            description = "지정 계좌에 금액을 입금한다. 금액은 문자열로 전달 (JSON 부동소수점 회피).")
    public ApiResponse<AccountResponse> deposit(
            @Parameter(description = "계좌번호", example = "110-01-1000000-X")
            @PathVariable String accountNumber,
            @Valid @RequestBody DepositRequest request) {
        Account account = accountService.deposit(
                new AccountNumber(accountNumber),
                new BigDecimal(request.amount()));
        return ApiResponse.success(AccountResponse.from(account));
    }

    @PostMapping("/{accountNumber}/withdrawals")
    @Operation(summary = "출금",
            description = "지정 계좌에서 금액을 출금한다. 비관적 락으로 동시 출금을 직렬화.")
    public ApiResponse<AccountResponse> withdraw(
            @Parameter(description = "계좌번호", example = "110-01-1000000-X")
            @PathVariable String accountNumber,
            @Valid @RequestBody WithdrawRequest request) {
        Account account = accountService.withdraw(
                new AccountNumber(accountNumber),
                new BigDecimal(request.amount()));
        return ApiResponse.success(AccountResponse.from(account));
    }

    @GetMapping("/{accountNumber}/transactions")
    @Operation(summary = "계좌 거래 내역",
            description = "최신순 기본 20건 (최대 100).")
    public ApiResponse<List<TransactionResponse>> transactions(
            @Parameter(description = "계좌번호", example = "110-01-1000000-X")
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "20") int limit) {
        int clamped = Math.min(Math.max(limit, 1), 100);
        List<TransactionResponse> items = accountService
                .recentTransactions(new AccountNumber(accountNumber), clamped)
                .stream()
                .map(TransactionResponse::from)
                .toList();
        return ApiResponse.success(items);
    }
}

