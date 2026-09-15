package com.kyrodatatech.banking.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * ================================================================
 * OpenApiConfig — Swagger UI / OpenAPI Documentation Setup
 * ================================================================
 *
 * Configures the auto-generated Swagger UI for this API.
 * Swagger UI is an interactive web interface to test all API endpoints.
 *
 * ACCESS SWAGGER UI AT: http://localhost:8080/swagger-ui.html
 *
 * WHAT THIS CONFIGURES:
 * 1. API title, description, version, and contact info
 * 2. JWT Bearer token authentication button in Swagger UI
 *    (so you can test secured endpoints by pasting your JWT token)
 * 3. Server URL (localhost for development)
 *
 * HOW TO USE SWAGGER UI:
 * 1. Open http://localhost:8080/swagger-ui.html
 * 2. Click "POST /api/auth/login" → Try it out → Execute
 * 3. Copy the "accessToken" from the response
 * 4. Click "Authorize" button (top right) → Paste token → Authorize
 * 5. Now all secured endpoints are accessible from Swagger UI
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configures the main OpenAPI documentation object.
     *
     * @return Fully configured OpenAPI bean
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // API metadata shown in Swagger UI header
                .info(new Info()
                    .title("CMS Finance & Banking System API")
                    .version("1.0.0")
                    .description("""
                        **Production-ready Banking Platform API**
                        
                        This API powers the CMS Banking Platform with:
                        - **JWT Authentication** — Login with email/password
                        - **Google OAuth2** — Login with Google
                        - **Role-Based Access Control** — 30+ roles from Bank Super Admin to Corporate Maker
                        - **Maker-Checker Workflow** — 4-eyes approval for all critical operations
                        - **Payment Modules** — NEFT, RTGS, IMPS, UPI, SWIFT, ACH, Bulk
                        - **Collection Modules** — Virtual Accounts, Direct Debit, QR, Receivables
                        - **Liquidity Management** — Sweeping, Pooling, Inter-company
                        - **LLM Integration** — AI-powered transaction analysis
                        
                        **Authentication Steps:**
                        1. POST /api/auth/login to get your access token
                        2. Click **Authorize** button below → Paste token
                        3. All secured endpoints will now work
                        """)
                    .contact(new Contact()
                        .name("KyroDataTech Engineering")
                        .email("engineering@kyrodatatech.com")
                        .url("https://kyrodatatech.com"))
                    .license(new License()
                        .name("Proprietary")
                        .url("https://kyrodatatech.com/license"))
                )
                // Server URLs shown in the Swagger UI
                .servers(List.of(
                    new Server()
                        .url("http://localhost:8080")
                        .description("Local Development Server"),
                    new Server()
                        .url("https://api.kyrobank.com")
                        .description("Production Server")
                ))
                // Add JWT Bearer token authentication to Swagger UI
                .components(new Components()
                    .addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("Enter your JWT token. Get it from POST /api/auth/login")
                    )
                )
                // Apply bearer auth globally to all endpoints
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
