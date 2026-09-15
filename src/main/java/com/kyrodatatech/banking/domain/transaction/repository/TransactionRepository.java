package com.kyrodatatech.banking.domain.transaction.repository;

import com.kyrodatatech.banking.domain.transaction.entity.Transaction;
import com.kyrodatatech.banking.domain.transaction.enums.TransactionStatus;
import com.kyrodatatech.banking.domain.transaction.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ================================================================
 * TransactionRepository — Database Access for Transactions
 * ================================================================
 *
 * Provides CRUD and search operations for the 'transactions' table.
 *
 * NOTE ON PAGINATION:
 * Banking systems deal with thousands of transactions per day.
 * We use Pageable to return transactions in pages (e.g., 20 at a time)
 * instead of loading ALL transactions into memory at once.
 *
 * Example usage:
 *   Pageable pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending());
 *   Page<Transaction> page = repo.findByStatus(PENDING, pageable);
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    /**
     * Find transaction by its reference number (UTR/TxnRef).
     * Used for transaction inquiries and status checks.
     */
    Optional<Transaction> findByTransactionRefNo(String transactionRefNo);

    /**
     * Find all transactions for a specific debit account.
     * Used for account statement generation.
     *
     * @param accountNo The account number
     * @param pageable  Pagination config
     * @return Page of transactions
     */
    Page<Transaction> findByDebitAccountNo(String accountNo, Pageable pageable);

    /**
     * Find transactions by status (paginated).
     * Used by operations team to see PENDING or FAILED transactions.
     */
    Page<Transaction> findByStatus(TransactionStatus status, Pageable pageable);

    /**
     * Find transactions by type (paginated).
     * Used to filter by payment type (NEFT, RTGS, UPI, etc.)
     */
    Page<Transaction> findByTransactionType(TransactionType type, Pageable pageable);

    /**
     * Find transactions submitted by a specific user (Maker's transactions).
     */
    List<Transaction> findByCreatedById(UUID userId);

    /**
     * Custom query for AML/Risk reporting:
     * Find all transactions above a certain amount within a date range.
     * Used by compliance officers to review large transactions.
     */
    @Query("SELECT t FROM Transaction t " +
           "WHERE t.amount >= :threshold " +
           "AND t.createdAt BETWEEN :from AND :to " +
           "ORDER BY t.amount DESC")
    List<Transaction> findLargeTransactions(BigDecimal threshold,
                                            LocalDateTime from,
                                            LocalDateTime to);

    /**
     * Count transactions by status and type.
     * Used for the dashboard/analytics.
     */
    long countByStatusAndTransactionType(TransactionStatus status, TransactionType type);
}
