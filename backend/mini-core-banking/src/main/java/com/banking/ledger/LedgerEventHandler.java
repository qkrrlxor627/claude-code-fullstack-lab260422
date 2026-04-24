package com.banking.ledger;

import com.banking.ledger.domain.LedgerEntry;
import com.banking.ledger.domain.LedgerSide;
import com.banking.ledger.port.LedgerEntryRepository;
import com.banking.transaction.domain.TransactionType;
import com.banking.transaction.event.TransactionCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Clock;

/**
 * 거래 커밋 완료 후 원장(LedgerEntry) 에 복식부기 분개를 기록한다.
 *
 * <p><b>비동기 + AFTER_COMMIT</b>:
 * <ul>
 *   <li>원본 거래 트랜잭션이 커밋된 후에만 실행 → 거래가 롤백되면 원장도 기록 안 됨.</li>
 *   <li>{@code @Async} 로 별도 스레드에서 실행 → 고객 요청 응답 속도에 영향 없음.</li>
 *   <li>예외 발생 시 원본 거래는 이미 커밋됐으므로 롤백 불가. 로깅만 수행.</li>
 * </ul>
 *
 * <p><b>분개 규칙 (토이 수준 단순화)</b>:
 * <ul>
 *   <li>입금: (차변) 현금자산 / (대변) 고객 계좌</li>
 *   <li>출금: (차변) 고객 계좌 / (대변) 현금자산</li>
 *   <li>이체 OUT: (차변) 출금 계좌 / (대변) 수취 계좌</li>
 *   <li>이체 IN: skip (OUT 측에서 이미 차·대변 한 쌍 기록)</li>
 * </ul>
 * 위 규칙으로 transaction_id 당 차변 합 = 대변 합 = amount 가 항상 성립.
 */
@Component
public class LedgerEventHandler {

    private static final Logger log = LoggerFactory.getLogger(LedgerEventHandler.class);

    private final LedgerEntryRepository ledgerRepository;
    private final Clock clock;

    public LedgerEventHandler(LedgerEntryRepository ledgerRepository, Clock clock) {
        this.ledgerRepository = ledgerRepository;
        this.clock = clock;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onTransactionCompleted(TransactionCompletedEvent event) {
        try {
            switch (event.type()) {
                case DEPOSIT -> {
                    // 은행 자산 증가 (차변), 고객 예수금 증가 = 대변
                    save(event.transactionId(), LedgerEntry.CASH_ASSET, LedgerSide.DEBIT,
                            event.amount(), "deposit");
                    save(event.transactionId(), event.accountNumber(), LedgerSide.CREDIT,
                            event.amount(), "deposit");
                }
                case WITHDRAWAL -> {
                    // 고객 예수금 감소 = 차변, 은행 자산 감소 = 대변
                    save(event.transactionId(), event.accountNumber(), LedgerSide.DEBIT,
                            event.amount(), "withdrawal");
                    save(event.transactionId(), LedgerEntry.CASH_ASSET, LedgerSide.CREDIT,
                            event.amount(), "withdrawal");
                }
                case TRANSFER_OUT -> {
                    // 두 계좌 간 이체: 출금 계좌 차변, 수취 계좌 대변
                    // IN 측 이벤트는 skip
                    save(event.transactionId(), event.accountNumber(), LedgerSide.DEBIT,
                            event.amount(), "transfer_out:" + event.transferId());
                    save(event.transactionId(), event.counterpartyAccount(), LedgerSide.CREDIT,
                            event.amount(), "transfer_out:" + event.transferId());
                }
                case TRANSFER_IN -> {
                    // OUT 쪽에서 이미 처리됨 — 중복 방지
                }
            }
        } catch (Exception ex) {
            log.error("원장 분개 실패 — transactionId={}, type={}, error={}",
                    event.transactionId(), event.type(), ex.getMessage(), ex);
            // 원본 거래는 이미 커밋. 재시도 큐/실패 테이블은 추후 스코프.
        }
    }

    private void save(Long transactionId, String accountCode, LedgerSide side,
                      java.math.BigDecimal amount, String memo) {
        ledgerRepository.save(LedgerEntry.create(
                transactionId, accountCode, side, amount, memo, clock));
    }
}
