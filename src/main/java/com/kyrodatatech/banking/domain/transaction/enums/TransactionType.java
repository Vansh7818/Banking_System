package com.kyrodatatech.banking.domain.transaction.enums;

/**
 * ================================================================
 * TransactionType — All Payment, Collection, and Liquidity Types
 * ================================================================
 *
 * Each payment method has different routing, limits, cut-off times,
 * and settlement cycles. This enum categorizes all of them.
 *
 * PAYMENT TYPES:
 *   - INTERNAL: Between accounts in the same bank (instant, free)
 *   - NEFT:     National Electronic Funds Transfer (batch, T+0)
 *   - RTGS:     Real Time Gross Settlement (instant, high-value >₹2L)
 *   - IMPS:     Immediate Payment Service (24x7 instant, up to ₹5L)
 *   - UPI:      Unified Payments Interface (24x7, via VPA)
 *   - SWIFT:    Cross-border international wire transfers
 *   - ACH:      Automated Clearing House (bulk batch)
 *   - BULK:     Bulk payment file processing (payroll, vendor payments)
 *
 * COLLECTION TYPES:
 *   - VIRTUAL_ACCOUNT: Virtual account number for collections
 *   - DIRECT_DEBIT:    Auto-debit from customer accounts
 *   - QR_COLLECTION:   QR code based payment collection
 *   - RECEIVABLES:     Invoice-based receivables management
 *
 * LIQUIDITY TYPES:
 *   - SWEEP:            Auto-sweeping surplus funds to a master account
 *   - POOL:             Notional pooling — aggregate balances
 *   - INTER_COMPANY:    Transfers between group companies
 */
public enum TransactionType {

    // ---- Payment Types ----
    INTERNAL_TRANSFER,
    NEFT,
    RTGS,
    IMPS,
    UPI,
    SWIFT,
    ACH,
    BULK_PAYMENT,

    // ---- Collection Types ----
    VIRTUAL_ACCOUNT,
    DIRECT_DEBIT,
    QR_COLLECTION,
    RECEIVABLES,

    // ---- Liquidity Types ----
    SWEEP,
    POOL,
    INTER_COMPANY_TRANSFER,

    // ---- Information Services ----
    ACCOUNT_STATEMENT,
    BALANCE_INQUIRY,
    TRANSACTION_INQUIRY
}
