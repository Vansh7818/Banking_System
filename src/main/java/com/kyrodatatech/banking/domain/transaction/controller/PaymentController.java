package com.kyrodatatech.banking.domain.transaction.controller;

import com.kyrodatatech.banking.domain.transaction.entity.Transaction;
import com.kyrodatatech.banking.domain.transaction.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/internal")
    public ResponseEntity<Transaction> internalTransfer(@RequestBody Transaction transaction) {
        return ResponseEntity.ok(paymentService.initiateInternalTransfer(transaction));
    }

    @PostMapping("/neft")
    public ResponseEntity<Transaction> neft(@RequestBody Transaction transaction) {
        return ResponseEntity.ok(paymentService.initiateNeft(transaction));
    }

    @PostMapping("/rtgs")
    public ResponseEntity<Transaction> rtgs(@RequestBody Transaction transaction) {
        return ResponseEntity.ok(paymentService.initiateRtgs(transaction));
    }

    @PostMapping("/imps")
    public ResponseEntity<Transaction> imps(@RequestBody Transaction transaction) {
        return ResponseEntity.ok(paymentService.initiateImps(transaction));
    }
}
