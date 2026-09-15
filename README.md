# CMS Finance & Banking System

A production-ready Enterprise Banking Platform built with **Spring Boot 3.3.2** and **Java 21**. 
This system uses a strict **Domain-Driven Design (DDD)** architecture and implements a comprehensive 4-eyes Maker-Checker approval workflow.

## 🏗 Architecture & Modules

The project is structured into distinct domain packages:

- **`domain.auth`**: Stateless JWT Authentication, Google OAuth2 integration, and custom UserDetailsService.
- **`domain.user`**: Role-Based Access Control (RBAC) with 30+ internal and corporate roles, corporate hierarchy (Groups & Legal Entities), and the `MakerCheckerService`.
- **`domain.transaction`**: Full transaction pipeline (Validation → Sanctions → AML → Approval → Processing). Includes shell implementations for:
  - **Payments**: NEFT, RTGS, IMPS, UPI, SWIFT, ACH, Bulk.
  - **Collections**: Virtual Accounts, Direct Debit (NACH mandates), QR Generation, Receivables.
  - **Liquidity**: Cash Sweeping, Notional Pooling, Inter-Company Transfers.
- **`domain.llm`**: Local LLM integration (Ollama-ready) for advanced transaction analysis.

## 🚀 Getting Started

### Prerequisites
- **Java 21**
- **Maven**
- **Docker** (optional, for PostgreSQL)

### Running Locally (Quick Start)

The application is currently configured to use an in-memory **H2 database** by default for immediate testing without external dependencies. 

1. **Compile & Build:**
   ```bash
   mvn clean install -DskipTests
   ```

2. **Run the Server:**
   ```bash
   mvn spring-boot:run
   ```

3. **Access the Backend API:**
   Once the server starts on port `8080`, navigate to the Swagger UI:
   [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Running the Frontend

The project includes a professional React dashboard built with Vite, TypeScript, and Tailwind CSS.

1. **Install Dependencies:**
   ```bash
   cd frontend
   npm install
   ```

2. **Start the Development Server:**
   ```bash
   npm run dev
   ```

3. **Access the Dashboard:**
   Open your browser and navigate to:
   [http://localhost:3000/](http://localhost:3000/)

### Running with PostgreSQL (Production-like)

To switch from the H2 memory database to PostgreSQL:

1. Start the PostgreSQL container:
   ```bash
   docker-compose up -d
   ```
2. Update `application.yml` to use the PostgreSQL datasource (remove the `h2` block and configure `spring.datasource.url`).
3. Restart the application.

### Configuring Google Login

Create a Google OAuth 2.0 Web application client in Google Cloud Console. Add
`http://localhost:8080/login/oauth2/code/google` as an authorized redirect URI,
then start the backend with these environment variables:

```powershell
$env:GOOGLE_CLIENT_ID = "your-client-id.apps.googleusercontent.com"
$env:GOOGLE_CLIENT_SECRET = "your-client-secret"
```

The frontend starts Google login at `http://localhost:8080/oauth2/authorization/google`.
Google login cannot work while these variables are empty or contain placeholder values.

## 🔒 Security & Workflow

- **JWT Authentication**: Pass the `Authorization: Bearer <token>` header to access secured endpoints.
- **Maker-Checker Engine**: Operations like creating users or initiating payments are first submitted as `PENDING_APPROVAL` requests. A different user with the appropriate `CHECKER` or `ADMIN` role must approve them. Self-approval is strictly forbidden.

## 🤝 Contribution Guidelines
Follow the DDD structure. When adding new payment gateways (e.g., NPCI, SWIFT network), implement them as strategy beans within `domain.transaction.service`.
