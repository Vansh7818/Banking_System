package com.kyrodatatech.banking.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * ================================================================
 * RegisterRequest — DTO for User Registration API
 * ================================================================
 *
 * Captures data for creating a new user account.
 * Used by the BANK_USER_ADMIN (Maker) to register bank staff,
 * and by CORP_USER_ADMIN to register corporate users.
 *
 * ENDPOINT: POST /api/auth/register
 *
 * NOTE: Registered users are created with PENDING_APPROVAL status.
 * They cannot log in until a CHECKER approves their account.
 *
 * EXPECTED JSON:
 * {
 *   "fullName": "John Doe",
 *   "email": "john.doe@kyrobank.com",
 *   "password": "SecurePass@123",
 *   "employeeId": "EMP001",
 *   "phoneNumber": "+919876543210"
 * }
 */
@Data
public class RegisterRequest {

    /**
     * Full legal name of the user.
     * Between 2 and 150 characters.
     */
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 150, message = "Full name must be between 2 and 150 characters")
    private String fullName;

    /**
     * Email address — will be used as the login username.
     * Must be unique across the entire system.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    /**
     * Password — must be at least 8 characters.
     * Will be BCrypt hashed before storage. Plain text is NEVER stored.
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    /**
     * Optional employee or staff ID.
     * Used for bank staff. Can be null for external/corporate users.
     */
    private String employeeId;

    /**
     * Optional phone number for OTP / 2FA notifications.
     * Format: +CountryCode followed by number (e.g., +919876543210)
     */
    private String phoneNumber;
}
