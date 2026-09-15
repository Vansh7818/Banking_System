package com.kyrodatatech.banking.domain.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * ================================================================
 * JwtAuthFilter — JWT Authentication Filter
 * ================================================================
 *
 * This filter runs ONCE for every HTTP request to our API.
 * It checks if the request has a valid JWT token in the Authorization header.
 *
 * HOW THE FILTER WORKS:
 * ─────────────────────────────────────────────────────────────
 * For EVERY incoming request:
 *
 *  1. Extract JWT from Authorization header
 *     Request header: "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
 *
 *  2. Validate the JWT using JwtTokenProvider
 *     - Is the signature valid?
 *     - Has it expired?
 *
 *  3. Load the user from database using email from token
 *
 *  4. Set authentication in SecurityContext
 *     - Now Spring Security knows who this user is
 *     - @PreAuthorize checks will work for this request
 *
 *  5. Pass request to the next filter / controller
 * ─────────────────────────────────────────────────────────────
 *
 * EXTENDS OncePerRequestFilter:
 *   Ensures this filter runs exactly ONCE per request,
 *   even if the filter chain is called multiple times internally.
 *
 * @Component — Spring manages this as a bean and registers it in the filter chain
 */
@Component
@RequiredArgsConstructor // Lombok: generates constructor for all final fields
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    /** JWT utility for token validation and claim extraction */
    private final JwtTokenProvider jwtTokenProvider;

    /** Spring Security service to load user from database */
    private final UserDetailsService userDetailsService;

    /**
     * The main filter method — runs for every HTTP request.
     *
     * @param request     Incoming HTTP request
     * @param response    HTTP response (we can modify headers here if needed)
     * @param filterChain Chain of filters to continue after this one
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            // Step 1: Extract JWT token from the Authorization header
            String jwt = extractJwtFromRequest(request);

            // Step 2: Only proceed if we actually have a token
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {

                // Step 3: Extract the username (email) from the token
                String username = jwtTokenProvider.extractUsername(jwt);

                // Step 4: Only authenticate if not already authenticated in this request
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    // Load user details from database
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // Step 5: Create authentication token for Spring Security
                    // UsernamePasswordAuthenticationToken = "this user is authenticated"
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,              // Principal (the user object)
                                    null,                     // Credentials (null after auth)
                                    userDetails.getAuthorities() // Roles/permissions
                            );

                    // Attach the HTTP request details (IP address, etc.) to the auth token
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Step 6: Tell Spring Security this request is authenticated
                    // After this, @PreAuthorize checks will work for this request
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("Authenticated user: {} | Path: {}", username, request.getRequestURI());
                }
            }
        } catch (Exception e) {
            // Log the error but don't stop the filter chain
            // The request will proceed as unauthenticated and get 401 from SecurityConfig
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        // Step 7: Continue to the next filter (or the controller if this is last)
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the JWT string from the Authorization header.
     *
     * The Authorization header format is: "Bearer {jwt_token}"
     * We strip the "Bearer " prefix to get just the token.
     *
     * @param request The HTTP request
     * @return The JWT string, or null if not present
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        // Check if the header exists and starts with "Bearer "
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // Extract everything after "Bearer " (7 characters)
            return bearerToken.substring(7);
        }

        return null;
    }
}
