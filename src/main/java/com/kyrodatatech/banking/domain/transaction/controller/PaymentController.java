package com.kyrodatatech.banking.domain.transaction.controller;

import com.kyrodatatech.banking.domain.transaction.entity.Transaction;
import com.kyrodatatech.banking.domain.transaction.service.PaymentService;
import com.kyrodatatech.banking.domain.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import com.kyrodatatech.banking.domain.user.entity.User;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final TransactionService transactionService;

    private Transaction prepare(Transaction transaction, Authentication authentication) {
        transaction.setTransactionRefNo(transactionService.generateRefNo());
        transaction.setCreatedBy((User) authentication.getPrincipal());
        return transaction;
    }

    @PostMapping("/internal")
    public ResponseEntity<Transaction> internalTransfer(@RequestBody Transaction transaction, Authentication authentication) {
        return ResponseEntity.ok(paymentService.initiateInternalTransfer(prepare(transaction, authentication)));
    }

    @PostMapping("/neft")
    public ResponseEntity<Transaction> neft(@RequestBody Transaction transaction, Authentication authentication) {
        return ResponseEntity.ok(paymentService.initiateNeft(prepare(transaction, authentication)));
    }

    @PostMapping("/rtgs")
    public ResponseEntity<Transaction> rtgs(@RequestBody Transaction transaction, Authentication authentication) {
        return ResponseEntity.ok(paymentService.initiateRtgs(prepare(transaction, authentication)));
    }

    @PostMapping("/imps")
    public ResponseEntity<Transaction> imps(@RequestBody Transaction transaction, Authentication authentication) {
        return ResponseEntity.ok(paymentService.initiateImps(prepare(transaction, authentication)));
    }

    @PostMapping("/upi")
    public ResponseEntity<Transaction> upi(@RequestBody Transaction transaction, Authentication authentication) {
        return ResponseEntity.ok(paymentService.initiateUpi(prepare(transaction, authentication)));
    }

    @PostMapping("/swift")
    public ResponseEntity<Transaction> swift(@RequestBody Transaction transaction, Authentication authentication) {
        return ResponseEntity.ok(paymentService.initiateSwift(prepare(transaction, authentication)));
    }

    @PostMapping("/ach")
    public ResponseEntity<Transaction> ach(@RequestBody Transaction transaction, Authentication authentication) {
        return ResponseEntity.ok(paymentService.initiateAch(prepare(transaction, authentication)));
    }

    @PostMapping("/bulk")
    public ResponseEntity<Transaction> bulk(@RequestBody Transaction transaction, Authentication authentication) {
        return ResponseEntity.ok(paymentService.initiateBulkPayment(prepare(transaction, authentication)));
    }
}
