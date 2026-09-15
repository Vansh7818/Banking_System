package com.kyrodatatech.banking.domain.transaction.service;

import com.kyrodatatech.banking.domain.transaction.entity.Transaction;
import com.kyrodatatech.banking.domain.transaction.enums.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * ================================================================
 * CollectionService — Collections & Receivables Business Logic
 * ================================================================
 *
 * Collections refer to RECEIVING money from customers/debtors.
 * Unlike payments (outgoing), collections are incoming fund flows.
 *
 * MODULES:
 * ─────────────────────────────────────────────────────────────
 * 1. VIRTUAL ACCOUNTS
 *    What: A virtual bank account number assigned to each customer/invoice
 *    Why:  Auto-reconcile incoming payments — no manual matching needed
 *    Use:  E-commerce, subscription billing, invoice collections
 *    How:  Customer pays to their unique virtual IBAN/account number
 *         → Payment auto-tagged with customer/invoice reference
 *
 * 2. DIRECT DEBIT
 *    What: Auto-debit from customer's account on a schedule
 *    Why:  Automated recurring collections (loans, subscriptions)
 *    Use:  EMI collection, insurance premiums, rent payments
 *    How:  Customer signs a mandate → Bank debits on due dates
 *
 * 3. QR COLLECTIONS
 *    What: Generate QR codes for UPI/payment collection
 *    Why:  Point-of-sale and B2B invoice collection via UPI QR
 *    Use:  Retail stores, invoice payment, restaurant billing
 *    How:  Generate dynamic QR → Customer scans → UPI payment → Reconcile
 *
 * 4. RECEIVABLES MANAGEMENT
 *    What: Track and manage outstanding invoices/dues
 *    Why:  Reduces DSO (Days Sales Outstanding) and bad debts
 *    Use:  Corporate treasury, AR (Accounts Receivable) management
 *    How:  Invoice creation → Track payment → Auto-reconcile → Report
 * ─────────────────────────────────────────────────────────────
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CollectionService {

    private final TransactionService transactionService;

    // ─────────────────────────────────────────────────────────────
    // VIRTUAL ACCOUNTS
    // ─────────────────────────────────────────────────────────────

    /**
     * Creates a Virtual Account for a corporate customer.
     *
     * A Virtual Account is a unique bank account number assigned to a specific
     * customer or invoice. When that customer pays to this virtual account,
     * the payment is automatically tagged and reconciled.
     *
     * EXAMPLE:
     * Real account: 1234567890 (Tata Motors' actual account)
     * Virtual account for Invoice #001: VA-TATA-001 → funds go to 1234567890
     * Virtual account for Invoice #002: VA-TATA-002 → funds go to 1234567890
     * Each payment is automatically matched to the correct invoice!
     *
     * @param corporateId       UUID of the corporate creating the virtual account
     * @param customerReference Reference for the customer/invoice
     * @param description       Description of the virtual account purpose
     * @return Virtual account number created
     */
    public String createVirtualAccount(String corporateId, String customerReference, String description) {
        log.info("Creating virtual account for corporate: {} | Reference: {}",
                corporateId, customerReference);

        // Generate unique virtual account number
        String virtualAccountNo = "VA-" + corporateId.substring(0, 8).toUpperCase()
                                 + "-" + System.currentTimeMillis();

        // TODO: Register virtual account with bank's CBS (Core Banking System)
        // virtualAccountGateway.create(VirtualAccountRequest.builder()
        //     .corporateId(corporateId)
        //     .reference(customerReference)
        //     .description(description)
        //     .build());

        log.info("Virtual account created: {}", virtualAccountNo);
        return virtualAccountNo;
    }

    /**
     * Records an incoming payment received via a Virtual Account.
     * The system automatically reconciles it to the correct invoice/customer.
     *
     * @param transaction The incoming transaction (credit to virtual account)
     * @return Processed and reconciled transaction
     */
    public Transaction processVirtualAccountCollection(Transaction transaction) {
        log.info("Processing virtual account collection: ₹{} to {}",
                transaction.getAmount(), transaction.getCreditAccountNo());

        transaction.setTransactionType(TransactionType.VIRTUAL_ACCOUNT);

        // TODO: Auto-reconcile with the linked invoice
        // reconciliationService.matchAndReconcile(transaction.getCreditAccountNo(), transaction);

        return transactionService.submitTransaction(transaction);
    }

    // ─────────────────────────────────────────────────────────────
    // DIRECT DEBIT
    // ─────────────────────────────────────────────────────────────

    /**
     * Creates a Direct Debit mandate for recurring collections.
     *
     * A mandate authorizes the bank to automatically debit a customer's
     * account on specified dates without requiring a new authorization each time.
     *
     * MANDATE TYPES:
     * - E-NACH (Electronic National Automated Clearing House) — Digital mandate
     * - Physical NACH — Paper mandate with customer signature
     *
     * @param debitAccountNo    Customer's account to debit
     * @param ifscCode          Customer's bank IFSC
     * @param amount            Amount to debit per cycle
     * @param frequency         "MONTHLY", "QUARTERLY", "WEEKLY"
     * @param startDate         When to start debiting
     * @param endDate           When the mandate expires
     * @return Mandate reference number
     */
    public String createDirectDebitMandate(String debitAccountNo, String ifscCode,
                                           BigDecimal amount, String frequency,
                                           java.time.LocalDate startDate,
                                           java.time.LocalDate endDate) {
        log.info("Creating E-NACH mandate: Account: {} | ₹{} | {} | {} to {}",
                debitAccountNo, amount, frequency, startDate, endDate);

        String mandateRef = "MANDATE-" + System.currentTimeMillis();

        // TODO: Submit to NPCI NACH API for mandate registration
        // nachGateway.createMandate(MandateRequest.builder()
        //     .accountNo(debitAccountNo)
        //     .ifscCode(ifscCode)
        //     .amount(amount)
        //     .frequency(frequency)
        //     .startDate(startDate)
        //     .endDate(endDate)
        //     .build());

        log.info("Direct debit mandate created: {}", mandateRef);
        return mandateRef;
    }

    /**
     * Executes a direct debit collection for an active mandate.
     * Called on the mandate's debit date.
     *
     * @param mandateRef  The mandate reference number
     * @param transaction Transaction details for this debit cycle
     * @return Processed debit transaction
     */
    public Transaction executeDirectDebit(String mandateRef, Transaction transaction) {
        log.info("Executing direct debit for mandate: {}", mandateRef);

        transaction.setTransactionType(TransactionType.DIRECT_DEBIT);
        transaction.setNarration("NACH Debit - Mandate: " + mandateRef);

        // TODO: Submit debit to NACH via NPCI
        // nachGateway.executeDebit(mandateRef, transaction.getAmount());

        return transactionService.submitTransaction(transaction);
    }

    // ─────────────────────────────────────────────────────────────
    // QR COLLECTIONS
    // ─────────────────────────────────────────────────────────────

    /**
     * Generates a dynamic UPI QR code for payment collection.
     *
     * Dynamic QR vs Static QR:
     * - STATIC: Fixed amount and merchant — used for retail counters
     * - DYNAMIC: Specific amount per transaction — used for invoices
     *
     * The QR code contains:
     * - Payee's UPI VPA (Virtual Payment Address)
     * - Amount (for dynamic QR)
     * - Transaction reference/invoice number
     *
     * @param vpa             Payee's UPI VPA (e.g., "merchant@okaxis")
     * @param amount          Amount to collect (null for static QR)
     * @param transactionNote Invoice number or description
     * @return Base64-encoded QR code image (for display in web/mobile)
     */
    public String generateCollectionQr(String vpa, BigDecimal amount, String transactionNote) {
        log.info("Generating collection QR for VPA: {} | Amount: ₹{}", vpa, amount);

        // TODO: Generate actual UPI QR code
        // UpiQrRequest qrRequest = UpiQrRequest.builder()
        //     .vpa(vpa)
        //     .amount(amount)
        //     .transactionNote(transactionNote)
        //     .merchantCode("SHOP001")
        //     .build();
        // return upiGateway.generateQrCode(qrRequest);

        // Simulate QR payload (UPI URL format)
        String upiUrl = "upi://pay?pa=" + vpa
                + "&pn=Merchant"
                + (amount != null ? "&am=" + amount : "")
                + "&tn=" + transactionNote
                + "&cu=INR";

        log.info("QR generated for VPA: {}", vpa);
        return upiUrl; // In production, convert to QR image
    }

    // ─────────────────────────────────────────────────────────────
    // RECEIVABLES MANAGEMENT
    // ─────────────────────────────────────────────────────────────

    /**
     * Creates a receivable record (invoice tracking).
     *
     * Tracks outstanding invoices and their payment status.
     * When payment is received (via any channel), it's matched to the invoice.
     *
     * @param invoiceNo     Invoice number
     * @param debtorName    Customer/debtor name
     * @param amount        Invoice amount
     * @param dueDate       Payment due date
     * @return Receivable ID
     */
    public String createReceivable(String invoiceNo, String debtorName,
                                   BigDecimal amount, java.time.LocalDate dueDate) {
        log.info("Creating receivable: Invoice #{} | {} | ₹{} | Due: {}",
                invoiceNo, debtorName, amount, dueDate);

        // TODO: Save receivable to database
        // Receivable receivable = Receivable.builder()
        //     .invoiceNo(invoiceNo)
        //     .debtorName(debtorName)
        //     .amount(amount)
        //     .dueDate(dueDate)
        //     .status(ReceivableStatus.OUTSTANDING)
        //     .build();
        // return receivableRepository.save(receivable).getId();

        String receivableId = "RCV-" + System.currentTimeMillis();
        log.info("Receivable created: {}", receivableId);
        return receivableId;
    }
}
