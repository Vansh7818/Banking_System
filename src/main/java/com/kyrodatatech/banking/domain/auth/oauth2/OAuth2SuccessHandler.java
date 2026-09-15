package com.kyrodatatech.banking.domain.auth.oauth2;

import java.io.IOException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.kyrodatatech.banking.domain.auth.JwtTokenProvider;
import com.kyrodatatech.banking.domain.user.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * ================================================================
 * OAuth2SuccessHandler — Called After Successful Google OAuth2 Login
 * ================================================================
 *
 * After Google authenticates the user, Spring Security calls this handler.
 * We generate JWT tokens and return them as JSON to the client.
 *
 * WHY THIS HANDLER?
 *   The default Spring OAuth2 behavior redirects to a success URL.
 *   But since we're a REST API (not a server-side rendered app),
 *   we want to return JWT tokens as JSON, not do a page redirect.
 *
 * RESPONSE FORMAT:
 * {
 *   "accessToken": "eyJhbGci...",
 *   "refreshToken": "eyJhbGci...",
 *   "email": "user@gmail.com",
 *   ...
 * }
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

        @Value("${app.frontend-url}")
        private String frontendUrl;

    /**
     * Called by Spring Security when OAuth2 login succeeds.
     * Generates JWT tokens and writes them to the HTTP response.
     *
     * @param request    The HTTP request
     * @param response   The HTTP response — we write JWT JSON here
     * @param authentication The successful authentication (contains Google user info)
     */
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        log.info("OAuth2 login successful for: {}", email);

        // Load our full User entity (which implements UserDetails) from database
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found after OAuth2 login: " + email));

        // Generate JWT tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        String callbackUrl = frontendUrl + "/oauth2/callback#accessToken="
            + encode(accessToken) + "&refreshToken=" + encode(refreshToken)
            + "&userId=" + encode(user.getId().toString())
            + "&email=" + encode(user.getEmail())
            + "&fullName=" + encode(user.getFullName())
            + "&roles=" + encode(user.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.joining(",")));
        response.sendRedirect(callbackUrl);
    }

    private String encode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }
}
