package com.kyrodatatech.banking.domain.user.enums;

/**
 * ================================================================
 * RoleType — Defines ALL roles in the CMS Banking Platform
 * ================================================================
 *
 * This enum defines every single role in the system, organized by category.
 * Roles are stored in the 'roles' table and assigned to users.
 *
 * TWO MAIN USER CATEGORIES:
 *   1. BANK_* roles  — Internal bank staff (admins, operations, risk, etc.)
 *   2. CORP_*  roles — Corporate clients using the banking platform
 *
 * HOW IT WORKS (for interns):
 *   - Every User has a Set<Role>
 *   - Every Role has a RoleType (this enum)
 *   - Spring Security checks roles before allowing API access
 *   - Example: @PreAuthorize("hasRole('BANK_SUPER_ADMIN')")
 */
public enum RoleType {

    // ============================================================
    // BANK USER ROLES — Internal Bank Staff
    // ============================================================

    /**
     * BANK_SUPER_ADMIN — Top-level bank administrator.
     * Has access to everything in the system.
     * Can create other bank admins.
     */
    BANK_SUPER_ADMIN,

    /**
     * BANK_USER_ADMIN — Manages bank user accounts.
     * Can create, update, deactivate bank staff accounts.
     * Works with Maker-Checker: creates accounts that need CHECKER approval.
     */
    BANK_USER_ADMIN,

    /**
     * BANK_PRODUCT_ADMIN — Manages banking products.
     * Can configure payment types, limits, fees, etc.
     */
    BANK_PRODUCT_ADMIN,

    /**
     * BANK_CONFIG_ADMIN — Manages system configuration.
     * Can change system-level settings, holidays, cut-off times.
     */
    BANK_CONFIG_ADMIN,

    // ---- Operations ----

    /**
     * PAYMENT_OPERATIONS — Handles daily payment operations.
     * Can view, process, and investigate payment transactions.
     */
    PAYMENT_OPERATIONS,

    /**
     * COLLECTION_OPERATIONS — Handles collections and receivables.
     * Manages Direct Debit, Virtual Accounts, QR collections.
     */
    COLLECTION_OPERATIONS,

    /**
     * RECONCILIATION — Matches bank transactions to internal records.
     * Identifies mismatches and raises exceptions.
     */
    RECONCILIATION,

    /**
     * EXCEPTION_MANAGEMENT — Handles failed or stuck transactions.
     * Can retry, reverse, or escalate transactions.
     */
    EXCEPTION_MANAGEMENT,

    /**
     * TRANSACTION_INVESTIGATION — Deep-dives into specific transactions.
     * Can access full audit trail and internal logs.
     */
    TRANSACTION_INVESTIGATION,

    /**
     * FILE_PROCESSING — Handles bulk file uploads (ACH, NEFT batch files).
     */
    FILE_PROCESSING,

    // ---- Risk & Compliance ----

    /**
     * COMPLIANCE_OFFICER — Ensures regulatory compliance.
     * Reviews transactions for legal compliance (RBI, FATF, etc.).
     */
    COMPLIANCE_OFFICER,

    /**
     * AML_SANCTIONS_REVIEWER — Anti-Money Laundering reviewer.
     * Screens transactions against sanctions lists (OFAC, UN, EU).
     * Can block or clear suspicious transactions.
     */
    AML_SANCTIONS_REVIEWER,

    /**
     * RISK_OFFICER — Manages bank risk policies.
     * Sets risk thresholds, reviews high-value transactions.
     */
    RISK_OFFICER,

    /**
     * BANK_AUDITOR — Internal/External auditor for the bank.
     * READ-ONLY access to all transactions and audit logs.
     * Cannot modify anything.
     */
    BANK_AUDITOR,

    // ---- Customer Management ----

    /**
     * CORPORATE_ONBOARDING — Onboards new corporate clients.
     * Creates CorporateGroup and LegalEntity records.
     */
    CORPORATE_ONBOARDING,

    /**
     * IMPLEMENTATION_MANAGER — Sets up product configuration for corporates.
     * Configures payment channels, limits, workflows per corporate.
     */
    IMPLEMENTATION_MANAGER,

    /**
     * CUSTOMER_SUPPORT — First-line support for corporate users.
     * Can view accounts, transactions, raise internal tickets.
     */
    CUSTOMER_SUPPORT,

    // ============================================================
    // CORPORATE USER ROLES — Corporate Client Staff
    // ============================================================

    /**
     * CORP_ADMIN — Top admin for a corporate group.
     * Can manage all users and settings within their corporate group.
     */
    CORP_ADMIN,

    /**
     * CORP_USER_ADMIN — Manages corporate user accounts.
     * Creates/deactivates corporate staff users within their legal entity.
     */
    CORP_USER_ADMIN,

    /**
     * CORP_FINANCE_MANAGER — Finance / Treasury Manager.
     * Oversees all financial transactions. Approves large payments.
     */
    CORP_FINANCE_MANAGER,

    /**
     * CORP_MAKER — Creates transactions/instructions for approval.
     * The MAKER in the Maker-Checker workflow.
     * Creates payments that need CHECKER approval before processing.
     */
    CORP_MAKER,

    /**
     * CORP_CHECKER — Verifies and approves transactions created by MAKER.
     * The CHECKER in the Maker-Checker workflow.
     * Reviews and approves/rejects payment requests.
     */
    CORP_CHECKER,

    /**
     * CORP_APPROVER_L1 — First-level approver in multi-level approval.
     * Approves transactions up to L1 limit (e.g., up to ₹1 Lakh).
     */
    CORP_APPROVER_L1,

    /**
     * CORP_APPROVER_L2 — Second-level approver.
     * Approves transactions between L1 and L2 limits (e.g., ₹1L to ₹10L).
     */
    CORP_APPROVER_L2,

    /**
     * CORP_FINAL_AUTHORIZER — Final authority for large transactions.
     * Approves transactions above L2 limit (e.g., above ₹10L).
     * Usually a CFO or Finance Director.
     */
    CORP_FINAL_AUTHORIZER,

    /**
     * CORP_BENEFICIARY_MANAGER — Manages the list of beneficiaries.
     * Can add/edit/delete payee bank accounts.
     */
    CORP_BENEFICIARY_MANAGER,

    /**
     * CORP_PAYROLL_USER — Handles payroll processing.
     * Can upload salary files and initiate payroll payments.
     */
    CORP_PAYROLL_USER,

    /**
     * CORP_COLLECTION_USER — Manages corporate collections.
     * Sets up Direct Debit mandates, manages Virtual Accounts.
     */
    CORP_COLLECTION_USER,

    /**
     * CORP_RECONCILIATION_USER — Corporate reconciliation team.
     * Downloads statements, matches with internal accounting records.
     */
    CORP_RECONCILIATION_USER,

    /**
     * CORP_REPORTING_USER — Generates corporate financial reports.
     * Access to all report types: statements, transaction summary, etc.
     */
    CORP_REPORTING_USER,

    /**
     * CORP_VIEWER — Read-only access to corporate data.
     * Can view accounts and transactions but cannot initiate anything.
     */
    CORP_VIEWER,

    /**
     * CORP_AUDITOR — Corporate internal auditor.
     * READ-ONLY access to all corporate transactions and audit logs.
     */
    CORP_AUDITOR
}
