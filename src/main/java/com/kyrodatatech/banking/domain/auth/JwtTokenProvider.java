package com.kyrodatatech.banking.domain.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ================================================================
 * JwtTokenProvider — JWT Token Generation and Validation
 * ================================================================
 *
 * JWT (JSON Web Token) is a secure way to authenticate API requests.
 *
 * HOW JWT WORKS (for interns):
 * ─────────────────────────────────────────────────────────────
 * 1. USER LOGS IN: POST /api/auth/login with email + password
 * 2. SERVER GENERATES TOKEN: JwtTokenProvider creates a signed JWT
 * 3. CLIENT STORES TOKEN: Frontend stores the JWT in localStorage/cookie
 * 4. CLIENT SENDS TOKEN: Every request includes "Authorization: Bearer <token>"
 * 5. SERVER VALIDATES: JwtAuthFilter extracts and validates the token
 * 6. SERVER PROCESSES: If valid, the request proceeds to the controller
 * ─────────────────────────────────────────────────────────────
 *
 * JWT STRUCTURE:
 * A JWT has 3 parts separated by dots: HEADER.PAYLOAD.SIGNATURE
 *
 * HEADER: {"alg": "HS256", "typ": "JWT"}
 * PAYLOAD: {"sub": "user@bank.com", "roles": ["ROLE_CORP_MAKER"], "exp": 1234567890}
 * SIGNATURE: HMAC-SHA256(base64(header) + "." + base64(payload), secretKey)
 *
 * The SIGNATURE ensures the token hasn't been tampered with.
 * Only our server knows the secretKey, so only we can create valid tokens.
 *
 * WE CREATE TWO TYPES OF TOKENS:
 * - Access Token (24 hours): Used for API authentication
 * - Refresh Token (7 days): Used to get a new access token after expiry
 *
 * @Component — Makes this class a Spring bean (injectable everywhere)
 */
@Component
@Slf4j
public class JwtTokenProvider {

    /**
     * Secret key from application.yml (jwt.secret).
     * Used to sign and verify JWT tokens.
     * MUST be kept secret — compromise = anyone can forge tokens!
     */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * How long access tokens are valid (in milliseconds).
     * Default: 86400000 ms = 24 hours
     */
    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    /**
     * How long refresh tokens are valid (in milliseconds).
     * Default: 604800000 ms = 7 days
     */
    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    /**
     * The application name — added as the "issuer" claim in JWT.
     * Example: "cms-banking-system"
     */
    @Value("${jwt.issuer}")
    private String issuer;

    // ---- Token Generation ----

    /**
     * Generates an ACCESS TOKEN for a logged-in user.
     *
     * The token payload (claims) includes:
     *   - sub (subject): user's email address
     *   - roles: list of user's roles (e.g., "ROLE_CORP_MAKER")
     *   - iss (issuer): our application name
     *   - iat (issued at): current timestamp
     *   - exp (expiry): 24 hours from now
     *
     * @param userDetails The authenticated user object
     * @return Signed JWT string (e.g., "eyJhbGciOiJIUzI1NiJ9...")
     */
    public String generateAccessToken(UserDetails userDetails) {
        return generateToken(userDetails, accessTokenExpiration, "ACCESS");
    }

    /**
     * Generates a REFRESH TOKEN for the user.
     * Used to obtain a new access token without re-logging in.
     *
     * @param userDetails The authenticated user object
     * @return Signed refresh JWT string
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(userDetails, refreshTokenExpiration, "REFRESH");
    }

    /**
     * Core token generation method.
     * Builds and signs a JWT with the specified expiry duration.
     *
     * @param userDetails    User to generate token for
     * @param expirationMs   Token validity in milliseconds
     * @param tokenType      "ACCESS" or "REFRESH"
     * @return Signed JWT string
     */
    private String generateToken(UserDetails userDetails, long expirationMs, String tokenType) {
        // Collect the user's roles as a comma-separated string
        String roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // Extra claims to embed in the token payload
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("roles", roles);
        extraClaims.put("tokenType", tokenType);

        long now = System.currentTimeMillis();

        return Jwts.builder()
                .claims(extraClaims)                              // Custom claims (roles, etc.)
                .subject(userDetails.getUsername())               // "sub" = email
                .issuer(issuer)                                    // "iss" = app name
                .issuedAt(new Date(now))                          // "iat" = now
                .expiration(new Date(now + expirationMs))         // "exp" = now + duration
                .signWith(getSigningKey())                         // Sign with HMAC-SHA256
                .compact();                                        // Build the JWT string
    }

    // ---- Token Validation ----

    /**
     * Validates a JWT token.
     *
     * Checks:
     * 1. Signature is valid (not tampered with)
     * 2. Token has not expired
     * 3. Token was issued by our application
     *
     * @param token The JWT string to validate
     * @return true if valid, false if invalid/expired
     */
    public boolean validateToken(String token) {
        try {
            // parseSignedClaims() does all validation automatically
            // It throws an exception if token is invalid, expired, or tampered with
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.warn("JWT token has expired: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("JWT token is unsupported: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    // ---- Token Data Extraction ----

    /**
     * Extracts the username (email) from a JWT token.
     *
     * @param token The JWT string
     * @return The user's email address (the "sub" claim)
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extracts all claims (payload) from a JWT token.
     *
     * @param token The JWT string
     * @return Claims object containing all token payload data
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Checks if a token has expired.
     *
     * @param token The JWT string
     * @return true if the token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            return extractAllClaims(token).getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    // ---- Signing Key ----

    /**
     * Converts the secret string from application.yml into a cryptographic key.
     *
     * We use HMAC-SHA256 (HS256) algorithm.
     * The key must be at least 256 bits (32 characters) for HS256.
     *
     * @return SecretKey used for JWT signing and verification
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
