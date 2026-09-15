package com.kyrodatatech.banking.domain.auth.oauth2;

import com.kyrodatatech.banking.domain.user.entity.Role;
import com.kyrodatatech.banking.domain.user.entity.User;
import com.kyrodatatech.banking.domain.user.enums.RoleType;
import com.kyrodatatech.banking.domain.user.enums.UserStatus;
import com.kyrodatatech.banking.domain.user.repository.RoleRepository;
import com.kyrodatatech.banking.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * ================================================================
 * CustomOAuth2UserService — Handles Google OAuth2 Login
 * ================================================================
 *
 * When a user clicks "Login with Google", this service handles the
 * user information returned by Google and maps it to our system.
 *
 * GOOGLE OAUTH2 FLOW:
 * ─────────────────────────────────────────────────────────────
 * 1. User clicks "Login with Google" on our frontend
 * 2. User is redirected to Google's login page
 * 3. User grants permission to our app
 * 4. Google redirects back to: /login/oauth2/code/google
 * 5. Spring Security calls this service to load/create the user
 * 6. We either find the existing user or create a new one
 * 7. OAuth2SuccessHandler generates JWT tokens and returns them
 * ─────────────────────────────────────────────────────────────
 *
 * ATTRIBUTES FROM GOOGLE:
 *   - "sub": Google's unique user ID
 *   - "email": User's Google email
 *   - "name": User's full name
 *   - "picture": Profile picture URL
 *   - "email_verified": Whether Google verified the email
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /**
     * Loads or creates a user based on the OAuth2 data from Google.
     *
     * Called automatically by Spring Security after Google authentication succeeds.
     *
     * @param userRequest Contains Google's access token and OAuth2 provider info
     * @return OAuth2User object used by Spring Security for this session
     */
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Call the parent to load raw user attributes from Google's API
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // Extract user attributes from Google's response
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String provider = userRequest.getClientRegistration().getRegistrationId(); // "google"
        String oauth2Id = (String) attributes.get("sub");           // Google's unique user ID
        String email = (String) attributes.get("email");             // User's email
        String name = (String) attributes.get("name");               // User's full name

        log.info("OAuth2 login attempt: email={}, provider={}", email, provider);

        // Try to find an existing user with this email in our database
        User user = userRepository.findByEmail(email)
                .map(existingUser -> updateExistingOAuth2User(existingUser, oauth2Id, provider))
                .orElseGet(() -> createNewOAuth2User(email, name, oauth2Id, provider));

        return oAuth2User;
    }

    /**
     * Updates an existing user's OAuth2 details if they were already registered.
     * This handles the case where someone registered with email/password
     * and then tries to log in with Google (same email).
     */
    private User updateExistingOAuth2User(User existingUser, String oauth2Id, String provider) {
        existingUser.setOauth2Id(oauth2Id);
        existingUser.setOauth2Provider(provider);
        log.info("Updated OAuth2 details for existing user: {}", existingUser.getEmail());
        return userRepository.save(existingUser);
    }

    /**
     * Creates a NEW user account for first-time Google login users.
     *
     * OAuth2 users are automatically set to ACTIVE (no Maker-Checker needed
     * for social login — this is a configurable policy decision).
     * They get the CORP_VIEWER role by default.
     */
    private User createNewOAuth2User(String email, String name, String oauth2Id, String provider) {
        log.info("Creating new OAuth2 user: {}", email);

        // Give OAuth2 users a default viewer role
        // In production, you'd configure which roles OAuth2 users get
        Set<Role> defaultRoles = new HashSet<>();
        roleRepository.findByRoleType(RoleType.CORP_VIEWER)
                .ifPresent(defaultRoles::add);

        User newUser = User.builder()
                .fullName(name)
                .email(email)
                .oauth2Id(oauth2Id)
                .oauth2Provider(provider)
                .status(UserStatus.ACTIVE)   // OAuth2 users are auto-activated
                .roles(defaultRoles)
                .build();

        return userRepository.save(newUser);
    }
}
