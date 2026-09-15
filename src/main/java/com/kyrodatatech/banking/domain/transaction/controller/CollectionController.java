package com.kyrodatatech.banking.domain.transaction.controller;

import com.kyrodatatech.banking.domain.transaction.entity.Transaction;
import com.kyrodatatech.banking.domain.transaction.service.CollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    @PostMapping("/virtual-account/process")
    public ResponseEntity<Transaction> processVirtualAccount(@RequestBody Transaction transaction) {
        return ResponseEntity.ok(collectionService.processVirtualAccountCollection(transaction));
    }

    @PostMapping("/qr/generate")
    public ResponseEntity<Map<String, String>> generateQr(@RequestBody Map<String, String> request) {
        String qr = collectionService.generateCollectionQr(request.get("vpa"), new java.math.BigDecimal(request.get("amount")), request.get("note"));
        return ResponseEntity.ok(Map.of("qrUrl", qr));
    }
}
