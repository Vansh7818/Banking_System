package com.kyrodatatech.banking.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ================================================================
 * ErrorResponse — Standard API Error Response DTO
 * ================================================================
 *
 * All API errors in our system return this JSON structure.
 * This ensures frontend developers and API consumers always know
 * exactly what fields to expect when an error occurs.
 *
 * EXAMPLE JSON RESPONSE:
 * {
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "Validation failed",
 *   "path": "/api/auth/login",
 *   "timestamp": "2024-09-15T09:47:00",
 *   "validationErrors": [
 *     "email: must not be blank",
 *     "password: size must be between 8 and 255"
 *   ]
 * }
 */
@Getter
@Builder
@AllArgsConstructor
public class ErrorResponse {

    /** HTTP status code (e.g., 400, 401, 403, 404, 500) */
    private int status;

    /** HTTP status reason (e.g., "Bad Request", "Unauthorized") */
    private String error;

    /** Specific, human-readable error message */
    private String message;

    /** The API path that triggered the error (e.g., "/api/auth/login") */
    private String path;

    /** When the error occurred */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * List of field-level validation errors.
     * Only present when request body validation fails.
     * Example: ["email: must be a well-formed email address"]
     */
    private List<String> validationErrors;
}
