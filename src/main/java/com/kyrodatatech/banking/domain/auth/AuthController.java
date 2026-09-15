package com.kyrodatatech.banking.domain.auth;

import com.kyrodatatech.banking.domain.auth.dto.AuthResponse;
import com.kyrodatatech.banking.domain.auth.dto.LoginRequest;
import com.kyrodatatech.banking.domain.auth.dto.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ================================================================
 * AuthController — Public Authentication REST API Endpoints
 * ================================================================
 *
 * This controller handles ALL authentication-related HTTP requests.
 * These endpoints are PUBLIC — no JWT token required to access them.
 *
 * BASE URL: /api/auth
 *
 * AVAILABLE ENDPOINTS:
 * ─────────────────────────────────────────────────────────────
 *  POST /api/auth/register     → Register new user account
 *  POST /api/auth/login        → Login, get JWT tokens
 *  POST /api/auth/refresh      → Get new access token
 *  GET  /api/auth/me           → Get current user info (JWT required)
 * ─────────────────────────────────────────────────────────────
 *
 * WHY @RestController?
 *   @RestController = @Controller + @ResponseBody
 *   @ResponseBody means every method's return value is automatically
 *   serialized to JSON and written to the HTTP response body.
 *
 * WHY @RequestMapping("/api/auth")?
 *   Sets the base URL prefix for all methods in this controller.
 *   So @PostMapping("/login") becomes POST /api/auth/login.
 *
 * SWAGGER UI: Visit http://localhost:8080/swagger-ui.html to test all endpoints.
 *
 * @Tag — Swagger annotation to group endpoints in Swagger UI
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration, login, and token management APIs")
public class AuthController {

    private final AuthService authService;

    /**
     * REGISTER — Create a new user account.
     *
     * POST /api/auth/register
     *
     * This does NOT return a JWT token.
     * The newly created account will have PENDING_APPROVAL status.
     * An Admin CHECKER must approve it before the user can login.
     *
     * REQUEST BODY:
     * {
     *   "fullName": "John Doe",
     *   "email": "john@kyrobank.com",
     *   "password": "SecurePass@123"
     * }
     *
     * @param request Validated registration data from request body
     * @return 201 Created with success message
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Register new user",
        description = "Creates a new user account with PENDING_APPROVAL status. Account must be approved by Admin Checker before first login."
    )
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        String message = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("message", message));
    }

    /**
     * LOGIN — Authenticate user and get JWT tokens.
     *
     * POST /api/auth/login
     *
     * REQUEST BODY:
     * {
     *   "email": "john@kyrobank.com",
     *   "password": "SecurePass@123"
     * }
     *
     * RESPONSE:
     * {
     *   "accessToken": "eyJhbGci...",
     *   "refreshToken": "eyJhbGci...",
     *   "roles": ["ROLE_CORP_MAKER"],
     *   ...
     * }
     *
     * @param request Login credentials (email + password)
     * @return 200 OK with JWT tokens and user details
     */
    @PostMapping("/login")
    @Operation(
        summary = "User login",
        description = "Authenticates credentials and returns JWT access + refresh tokens. Only ACTIVE accounts can login."
    )
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * REFRESH TOKEN — Get a new access token using a refresh token.
     *
     * POST /api/auth/refresh
     * Body: { "refreshToken": "eyJhbGci..." }
     *
     * Use this when the access token has expired (API returns 401).
     * The refresh token is valid for 7 days, so users don't need to
     * re-enter their password every 24 hours.
     *
     * @param body JSON body with "refreshToken" field
     * @return 200 OK with new access token
     */
    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh access token",
        description = "Generates a new access token using a valid refresh token. No re-login required."
    )
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    /**
     * HEALTH CHECK — Simple endpoint to verify the auth service is running.
     *
     * GET /api/auth/ping
     *
     * No authentication required. Useful for monitoring and load balancers.
     */
    @GetMapping("/ping")
    @Operation(summary = "Health check", description = "Returns OK if the auth service is running")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "CMS Banking Auth Service",
                "version", "1.0.0"
        ));
    }
}
