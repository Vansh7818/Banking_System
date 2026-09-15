package com.kyrodatatech.banking.domain.llm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * LlmService for interacting with local LLM.
 */
@Service
@Slf4j
public class LlmService {

    @Value("${llm.base-url}")
    private String baseUrl;

    @Value("${llm.model}")
    private String model;

    public String analyzeTransaction(String transactionDetails) {
        log.info("Analyzing transaction with LLM at {}: {}", baseUrl, transactionDetails);
        // Implement HTTP call to local LLM
        return "Transaction appears normal based on LLM analysis.";
    }
}
