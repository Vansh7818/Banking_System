package com.kyrodatatech.banking.domain.transaction.controller;

import com.kyrodatatech.banking.domain.transaction.entity.Transaction;
import com.kyrodatatech.banking.domain.transaction.service.LiquidityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/liquidity")
@RequiredArgsConstructor
public class LiquidityController {

    private final LiquidityService liquidityService;

    @PostMapping("/sweep/execute")
    public ResponseEntity<Transaction> executeSweep(@RequestBody Map<String, String> request) {
        Transaction tx = liquidityService.executeSweep(
            request.get("fromAccount"),
            request.get("toAccount"),
            new BigDecimal(request.get("amount"))
        );
        return ResponseEntity.ok(tx);
    }
}
