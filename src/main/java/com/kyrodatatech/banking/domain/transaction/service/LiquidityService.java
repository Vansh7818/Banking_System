package com.kyrodatatech.banking.domain.transaction.service;

import com.kyrodatatech.banking.domain.transaction.entity.Transaction;
import com.kyrodatatech.banking.domain.transaction.enums.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * ================================================================
 * LiquidityService — Cash & Liquidity Management Business Logic
 * ================================================================
 *
 * Liquidity management is about optimizing cash across multiple accounts
 * in a corporate group to minimize idle balances and borrowing costs.
 *
 * MODULES:
 * ─────────────────────────────────────────────────────────────
 * 1. SWEEPING (Cash Concentration)
 *    - Automatically moves surplus cash to a MASTER account
 *    - At end of day, all subsidiary accounts are "swept" clean
 *    - Corporate only keeps cash where it's needed
 *    - Maximizes interest earnings on consolidated balance
 *
 * 2. POOLING (Notional Pooling)
 *    - Aggregates balances from multiple accounts NOTIONALLY
 *    - No physical movement of cash
 *    - Bank calculates net position for interest purposes
 *    - Most suitable for large groups with many legal entities
 *
 * 3. INTER-COMPANY TRANSFER (ICT)
 *    - Transfers between different legal entities within a group
 *    - Example: Tata Motors lending cash to Tata Steel within group
 *    - Requires proper legal documentation (loan agreements, etc.)
 *    - Subject to transfer pricing and tax compliance
 * ─────────────────────────────────────────────────────────────
 *
 * BANKING BENEFIT:
 *   A corporate group with 50 subsidiaries each holding ₹1 Crore idle =
 *   ₹50 Crore idle cash earning no interest.
 *
 *   With SWEEP/POOL: Consolidated ₹50 Crore earns full interest in one account.
 *   Savings can be millions of rupees annually!
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LiquidityService {

    private final TransactionService transactionService;

    // ─────────────────────────────────────────────────────────────
    // SWEEPING
    // ─────────────────────────────────────────────────────────────

    /**
     * Sets up a Sweeping Structure for a corporate group.
     *
     * HOW SWEEPING WORKS:
     * 1. Accounts are linked in a HEADER-SUB structure
     * 2. Every day at EOD (End of Day) or a configured time:
     *    a. The system checks each sub-account balance
     *    b. If balance > Target Balance (e.g., ₹10,000)
     *       → Sweep EXCESS to Header/Master Account
     *    c. If balance < Minimum Balance (e.g., ₹5,000)
     *       → Reverse sweep: Move funds FROM Header to Sub-account
     * 3. Result: All excess cash consolidated in one master account
     *
     * SWEEP TYPES:
     * - ZERO BALANCE: Sweep entire balance to master (zero the account)
     * - TARGET BALANCE: Keep X amount, sweep the rest
     * - MINIMUM BALANCE: Ensure minimum balance; sweep surplus
     *
     * @param masterAccountNo  The central/header account to receive swept funds
     * @param subAccountNos    List of sub-accounts to sweep
     * @param targetBalance    Keep this much in each sub-account
     * @param sweepTime        When to execute daily sweep (e.g., "17:00")
     * @return Sweep structure ID
     */
    public String createSweepStructure(String masterAccountNo,
                                       List<String> subAccountNos,
                                       BigDecimal targetBalance,
                                       String sweepTime) {
        log.info("Creating sweep structure: Master: {} | {} sub-accounts | Target: ₹{} | Time: {}",
                masterAccountNo, subAccountNos.size(), targetBalance, sweepTime);

        // TODO: Configure sweep in core banking system
        // sweepGateway.createStructure(SweepStructureRequest.builder()
        //     .masterAccount(masterAccountNo)
        //     .subAccounts(subAccountNos)
        //     .targetBalance(targetBalance)
        //     .sweepSchedule(sweepTime)
        //     .sweepType("TARGET_BALANCE")
        //     .build());

        String sweepId = "SWEEP-" + System.currentTimeMillis();
        log.info("Sweep structure created: {}", sweepId);
        return sweepId;
    }

    /**
     * Executes an on-demand (manual) sweep.
     * In addition to the automated daily sweep, authorized users can
     * trigger immediate sweeps when needed.
     *
     * @param fromAccount Source account to sweep FROM
     * @param toAccount   Destination (master) account
     * @param amount      Amount to sweep (null = sweep entire available balance)
     * @return Sweep transaction record
     */
    public Transaction executeSweep(String fromAccount, String toAccount, BigDecimal amount) {
        log.info("Executing manual sweep: {} → {} | Amount: ₹{}", fromAccount, toAccount, amount);

        Transaction sweepTxn = Transaction.builder()
                .transactionType(TransactionType.SWEEP)
                .debitAccountNo(fromAccount)
                .creditAccountNo(toAccount)
                .amount(amount)
                .narration("Liquidity Sweep - Cash Concentration")
                .currency("INR")
                .transactionRefNo(transactionService.generateRefNo())
                .build();

        return transactionService.submitTransaction(sweepTxn);
    }

    // ─────────────────────────────────────────────────────────────
    // POOLING
    // ─────────────────────────────────────────────────────────────

    /**
     * Creates a Notional Pooling structure for a corporate group.
     *
     * HOW NOTIONAL POOLING WORKS:
     * - Multiple accounts are grouped in a "Pool"
     * - Bank calculates NET balance across ALL accounts for interest
     * - Individual accounts retain their own balances
     * - No physical movement of cash (unlike sweeping)
     *
     * EXAMPLE:
     * Tata Group Pool:
     *   TCS Account:        +₹100 Cr (surplus)
     *   Tata Motors Account: -₹40 Cr (overdraft)
     *   Tata Steel Account:  +₹20 Cr (surplus)
     *   Net position:        +₹80 Cr
     *
     * Interest paid on net +₹80 Cr (not individually on each account)
     * Tata Motors' overdraft is offset by TCS's surplus — NO interest charged!
     *
     * @param corporateGroupId  The corporate group ID for the pool
     * @param participantAccounts List of accounts to include in the pool
     * @param currency          Currency of the pool (INR, USD, etc.)
     * @return Pool ID
     */
    public String createNotionalPool(String corporateGroupId,
                                     List<String> participantAccounts,
                                     String currency) {
        log.info("Creating notional pool for group: {} | Accounts: {} | Currency: {}",
                corporateGroupId, participantAccounts.size(), currency);

        // TODO: Configure notional pool in bank's treasury system
        // poolGateway.createNotionalPool(PoolRequest.builder()
        //     .groupId(corporateGroupId)
        //     .accounts(participantAccounts)
        //     .currency(currency)
        //     .poolType("NOTIONAL")
        //     .build());

        String poolId = "POOL-" + currency + "-" + System.currentTimeMillis();
        log.info("Notional pool created: {}", poolId);
        return poolId;
    }

    /**
     * Gets the aggregated pool position for a corporate group.
     * Shows net balance, overdraft accounts, surplus accounts.
     *
     * @param poolId The notional pool ID
     * @return Pool position summary (placeholder - implement with DTO)
     */
    public String getPoolPosition(String poolId) {
        log.info("Fetching pool position for pool: {}", poolId);

        // TODO: Fetch real-time pool position from treasury system
        // return poolGateway.getPosition(poolId);

        return "Pool position for " + poolId + ": ₹80 Cr net credit position";
    }

    // ─────────────────────────────────────────────────────────────
    // INTER-COMPANY TRANSFER
    // ─────────────────────────────────────────────────────────────

    /**
     * Executes an Inter-Company Transfer (ICT) between group entities.
     *
     * Inter-company transfers happen when one entity in a group
     * lends money to another entity (called an "Intra-group loan").
     *
     * COMPLIANCE REQUIREMENTS:
     * - Must have a proper loan/transfer agreement
     * - Subject to transfer pricing regulations
     * - Must be at arm's length (market rate interest)
     * - Requires board approval for large amounts
     * - Tax implications (withholding tax on interest)
     *
     * ACCOUNTING TREATMENT:
     * - Entity A (lender): Records as Intercompany Loan Receivable
     * - Entity B (borrower): Records as Intercompany Loan Payable
     *
     * @param fromLegalEntityId  UUID of the lending legal entity
     * @param toLegalEntityId    UUID of the borrowing legal entity
     * @param transaction        Transfer transaction details
     * @param agreementRef       Reference to the intercompany agreement
     * @return Processed inter-company transfer transaction
     */
    public Transaction executeInterCompanyTransfer(String fromLegalEntityId,
                                                    String toLegalEntityId,
                                                    Transaction transaction,
                                                    String agreementRef) {
        log.info("Executing inter-company transfer: {} → {} | Amount: ₹{} | Agreement: {}",
                fromLegalEntityId, toLegalEntityId,
                transaction.getAmount(), agreementRef);

        transaction.setTransactionType(TransactionType.INTER_COMPANY_TRANSFER);
        transaction.setNarration("ICT: Intercompany Loan - Ref: " + agreementRef);
        transaction.setInternalRemarks(
                "From Entity: " + fromLegalEntityId + " | To Entity: " + toLegalEntityId
        );

        // TODO: Create intercompany accounting entries
        // accountingService.createIctEntries(fromLegalEntityId, toLegalEntityId, transaction);

        return transactionService.submitTransaction(transaction);
    }

    /**
     * Gets the intercompany balance between two entities.
     * Shows how much one entity owes to another within the group.
     *
     * @param fromEntityId The lending entity
     * @param toEntityId   The borrowing entity
     * @return Net intercompany position
     */
    public BigDecimal getIntercompanyBalance(String fromEntityId, String toEntityId) {
        log.info("Fetching intercompany balance: {} ↔ {}", fromEntityId, toEntityId);

        // TODO: Calculate from transaction history
        // return transactionRepository.calculateIntercompanyBalance(fromEntityId, toEntityId);

        return BigDecimal.ZERO; // Placeholder
    }
}
