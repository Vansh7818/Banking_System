package com.kyrodatatech.banking.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * ================================================================
 * AuthResponse — JWT Token Response DTO
 * ================================================================
 *
 * Returned to the client after a successful login or token refresh.
 *
 * EXAMPLE RESPONSE JSON:
 * {
 *   "userId": "550e8400-e29b-41d4-a716-446655440000",
 *   "email": "john.doe@kyrobank.com",
 *   "fullName": "John Doe",
 *   "roles": ["ROLE_CORP_MAKER", "ROLE_CORP_VIEWER"],
 *   "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
 *   "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
 *   "tokenType": "Bearer",
 *   "expiresIn": 86400
 * }
 *
 * The client should:
 * 1. Store the accessToken and refreshToken
 * 2. Send accessToken in every request: "Authorization: Bearer {accessToken}"
 * 3. When accessToken expires (HTTP 401), use refreshToken to get a new one:
 *    POST /api/auth/refresh with refreshToken
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /** The authenticated user's UUID */
    private UUID userId;

    /** The authenticated user's email */
    private String email;

    /** The authenticated user's full name */
    private String fullName;

    /**
     * List of role strings for the user.
     * Example: ["ROLE_BANK_SUPER_ADMIN", "ROLE_PAYMENT_OPERATIONS"]
     * The frontend can use this to show/hide UI elements.
     */
    private List<String> roles;

    /**
     * The JWT access token.
     * Valid for 24 hours (configurable in application.yml).
     * Send this in the Authorization header of every API request.
     */
    private String accessToken;

    /**
     * The JWT refresh token.
     * Valid for 7 days. Used to get a new access token when the old one expires.
     * Store this securely (HttpOnly cookie is recommended).
     */
    private String refreshToken;

    /**
     * Token type — always "Bearer" for JWT.
     * Usage: "Authorization: Bearer {accessToken}"
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * Access token validity in seconds.
     * Example: 86400 = 24 hours.
     * Frontend can use this to show "session expires in X hours" messages.
     */
    private long expiresIn;
}
