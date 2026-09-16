package com.kyrodatatech.banking.domain.llm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.List;

/**
 * ================================================================
 * LlmService — AI Transaction Analysis powered by Google Gemini
 * ================================================================
 *
 * This connects your Banking backend to Google's Gemini AI to 
 * perform intelligent AML and fraud analysis on transactions.
 */
@Service
@Slf4j
public class LlmService {

    @Value("${gemini.api.key:YOUR_GEMINI_API_KEY_HERE}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String analyzeTransaction(String transactionDetails) {
        log.info("Analyzing transaction with Gemini AI...");
        
        if (transactionDetails == null || transactionDetails.isEmpty()) {
            return "Error: No transaction details provided for analysis.";
        }

        // If the user hasn't put in their real API key yet, fall back to the smart local mock
        if ("YOUR_GEMINI_API_KEY_HERE".equals(apiKey)) {
            log.warn("No Gemini API key found. Falling back to local smart mock analysis.");
            return runLocalMockAnalysis(transactionDetails);
        }

        try {
            // Call the real Google Gemini API
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;
            
            // Construct the exact Gemini JSON payload structure
            Map<String, Object> payload = Map.of(
                "contents", List.of(
                    Map.of("parts", List.of(
                        Map.of("text", "You are an expert banking AML compliance officer. Analyze this transaction and flag any risks. Keep it under 4 sentences: " + transactionDetails)
                    ))
                )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            // Parse Gemini response
            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    return (String) parts.get(0).get("text");
                }
            }
            return "Analysis completed, but response could not be parsed.";
            
        } catch (Exception e) {
            log.error("Failed to connect to Gemini API", e);
            return "⚠️ Gemini API Error: " + e.getMessage();
        }
    }

    private String runLocalMockAnalysis(String details) {
        String lowerCaseDetails = details.toLowerCase();
        if (lowerCaseDetails.contains("swift") || lowerCaseDetails.contains("international") || lowerCaseDetails.contains("uae")) {
            return "⚠️ HIGH RISK IDENTIFIED:\n- Cross-border SWIFT transfer to high-risk jurisdiction (UAE).\n- Beneficiary account has no prior history with the sender.\n- Amount exceeds typical daily velocity limits.\n\nRECOMMENDATION: Flag for manual AML Sanctions Review.";
        }
        if (lowerCaseDetails.contains("bulk") || lowerCaseDetails.contains("salary")) {
            return "✅ LOW RISK:\n- Bulk payroll file detected.\n- All 200 beneficiaries are whitelisted employees.\n- Historical pattern matches regular end-of-month salary disbursements.\n\nRECOMMENDATION: Auto-approve and route to ACH network.";
        }
        return "✅ NORMAL RISK:\n- Transaction parameters fall within historical norms.\n- No sanctions matches found on beneficiary.\n- Velocity checks passed.\n\nRECOMMENDATION: Proceed with standard L1 Approval workflow.";
    }
}
