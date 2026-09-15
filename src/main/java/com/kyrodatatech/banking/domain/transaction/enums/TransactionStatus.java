package com.kyrodatatech.banking.domain.transaction.enums;

/**
 * ================================================================
 * TransactionStatus — Complete Transaction Lifecycle Status
 * ================================================================
 *
 * Mirrors the exact pipeline stages in banking:
 *
 *  SUBMITTED
 *    → VALIDATION_PASSED / VALIDATION_FAILED
 *    → SANCTIONS_CLEARED / SANCTIONS_BLOCKED
 *    → AML_CLEARED / AML_FLAGGED
 *    → PENDING_APPROVAL
 *    → APPROVED / REJECTED
 *    → PROCESSING / PROCESSING_FAILED
 *    → COMPLETED / REVERSED
 */
public enum TransactionStatus {

    /** Transaction submitted by MAKER, awaiting processing pipeline. */
    SUBMITTED,

    /** Passed all format/limit validation checks. */
    VALIDATION_PASSED,

    /** Failed validation (e.g., invalid IFSC, exceeds daily limit). */
    VALIDATION_FAILED,

    /** Sanctions/blacklist screening passed — safe to proceed. */
    SANCTIONS_CLEARED,

    /** Blocked by sanctions screening. Manual review required. */
    SANCTIONS_BLOCKED,

    /** AML risk check passed — not flagged as suspicious. */
    AML_CLEARED,

    /** Flagged by AML engine — needs compliance review. */
    AML_FLAGGED,

    /** Waiting for CHECKER/APPROVER to approve the transaction. */
    PENDING_APPROVAL,

    /** CHECKER has approved the transaction. Sending to bank network. */
    APPROVED,

    /** CHECKER has rejected the transaction. Funds not moved. */
    REJECTED,

    /** Transaction is being sent to the payment network (NEFT/RTGS etc.). */
    PROCESSING,

    /** Processing failed at the bank/payment network level. */
    PROCESSING_FAILED,

    /** Transaction successfully completed. Funds transferred. */
    COMPLETED,

    /** Transaction was reversed (refunded to sender). */
    REVERSED,

    /** Transaction cancelled before processing. */
    CANCELLED
}
