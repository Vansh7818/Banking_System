package com.kyrodatatech.banking.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * ================================================================
 * LoginRequest — Data Transfer Object for Login API
 * ================================================================
 *
 * A DTO (Data Transfer Object) carries data between the client and server.
 * This class captures the user's login credentials from the request body.
 *
 * ENDPOINT: POST /api/auth/login
 *
 * EXPECTED JSON REQUEST:
 * {
 *   "email": "john.doe@kyrobank.com",
 *   "password": "MySecurePassword123!"
 * }
 *
 * VALIDATION:
 * @NotBlank — field cannot be null or whitespace
 * @Email     — must be a valid email format
 * @Size      — enforces minimum/maximum length
 *
 * If validation fails, the request is rejected with 400 Bad Request
 * and the GlobalExceptionHandler returns the validation errors.
 */
@Data // Lombok: generates @Getter, @Setter, @ToString, @EqualsAndHashCode
public class LoginRequest {

    /**
     * User's email address — used as the login username.
     * Must be a valid email format (e.g., user@bank.com).
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    /**
     * User's password in plain text.
     * Minimum 8 characters (a basic security requirement).
     * This is compared against the BCrypt hash stored in the database.
     *
     * NOTE: HTTPS must be used in production so this is never sent in clear text.
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
