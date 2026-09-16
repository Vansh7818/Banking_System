package com.kyrodatatech.banking.domain.transaction.controller;

import com.kyrodatatech.banking.domain.transaction.entity.Transaction;
import com.kyrodatatech.banking.domain.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionRepository transactionRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('BANK_SUPER_ADMIN', 'PAYMENT_OPERATIONS', 'COLLECTION_OPERATIONS', 'RECONCILIATION', 'EXCEPTION_MANAGEMENT', 'TRANSACTION_INVESTIGATION', 'COMPLIANCE_OFFICER', 'AML_SANCTIONS_REVIEWER', 'RISK_OFFICER', 'BANK_AUDITOR', 'CUSTOMER_SUPPORT', 'CORP_ADMIN', 'CORP_FINANCE_MANAGER', 'CORP_RECONCILIATION_USER', 'CORP_REPORTING_USER', 'CORP_VIEWER', 'CORP_AUDITOR')")
    public ResponseEntity<List<Transaction>> getAll() {
        return ResponseEntity.ok(transactionRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")));
    }
}
