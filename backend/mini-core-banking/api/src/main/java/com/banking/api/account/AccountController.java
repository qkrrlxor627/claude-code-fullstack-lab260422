package com.banking.api.account;

import com.banking.api.account.dto.AccountResponse;
import com.banking.api.account.dto.OpenAccountRequest;
import com.banking.common.response.ApiResponse;
import com.banking.domain.account.Account;
import com.banking.domain.account.AccountNumber;
import com.banking.domain.account.AccountService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
}
