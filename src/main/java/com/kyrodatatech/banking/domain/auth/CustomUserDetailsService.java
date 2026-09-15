package com.kyrodatatech.banking.domain.auth;

import com.kyrodatatech.banking.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ================================================================
 * CustomUserDetailsService — Loads User from Database for Spring Security
 * ================================================================
 *
 * Spring Security's authentication system needs a way to look up users
 * from the database. This service implements UserDetailsService to do that.
 *
 * HOW IT'S USED:
 *   1. User submits email + password to /api/auth/login
 *   2. Spring Security calls loadUserByUsername(email)
 *   3. This method fetches the User entity from PostgreSQL
 *   4. Spring Security compares the provided password with stored BCrypt hash
 *   5. If match: authentication succeeds
 *
 * ALSO USED BY: JwtAuthFilter
 *   When validating JWT tokens, JwtAuthFilter calls loadUserByUsername(email)
 *   to get the current user's details and roles from the database.
 *
 * @Service — Marks this as a Spring service (business logic layer)
 * @Transactional(readOnly = true) — Opens a read-only DB transaction
 *   (optimizes performance for read queries)
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    /** Repository to query users from the database */
    private final UserRepository userRepository;

    /**
     * Loads a user by their email address (our "username").
     *
     * This method is called by Spring Security during:
     * - Login authentication
     * - JWT token validation (via JwtAuthFilter)
     *
     * @param email The user's email address (used as username in our system)
     * @return UserDetails object (our User entity implements UserDetails)
     * @throws UsernameNotFoundException if no user exists with that email
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Look up user from database by email
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email
                ));
        // Note: Our User entity implements UserDetails, so we can return it directly.
        // Spring Security will call user.getPassword(), user.getAuthorities(), etc.
    }
}
