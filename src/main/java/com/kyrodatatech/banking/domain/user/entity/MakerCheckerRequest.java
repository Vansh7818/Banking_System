package com.kyrodatatech.banking.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import com.kyrodatatech.banking.domain.user.enums.ApprovalStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ================================================================
 * MakerCheckerRequest — The Core Approval Workflow Entity
 * ================================================================
 *
 * This entity is the HEART of the Maker-Checker (4-eyes principle) system.
 * Every important action in the banking system goes through this workflow.
 *
 * WORKFLOW EXPLAINED (for interns):
 * ──────────────────────────────────────────────────────────────
 *
 *  STEP 1 — MAKER creates a request:
 *    POST /api/maker-checker/create-user
 *    → Creates a MakerCheckerRequest with status = PENDING
 *    → Creates a User with status = PENDING_APPROVAL
 *
 *  STEP 2 — CHECKER reviews the request:
 *    GET /api/maker-checker/pending  (sees all pending requests)
 *    → Checker reviews the data
 *
 *  STEP 3a — CHECKER approves:
 *    POST /api/maker-checker/{id}/approve
 *    → MakerCheckerRequest.status = APPROVED
 *    → User.status = ACTIVE (user can now login!)
 *
 *  STEP 3b — CHECKER rejects:
 *    POST /api/maker-checker/{id}/reject
 *    → MakerCheckerRequest.status = REJECTED
 *    → MakerCheckerRequest.rejectionReason = "Wrong department assigned"
 *    → Maker is notified
 *
 * ──────────────────────────────────────────────────────────────
 *
 * APPLIES TO:
 *   - User creation / modification
 *   - High-value payment authorization
 *   - Beneficiary addition
 *   - Configuration changes
 *
 * DATABASE TABLE: 'maker_checker_requests'
 */
@Entity
@Table(name = "maker_checker_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MakerCheckerRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // ---- Type of Action Being Requested ----

    /**
     * What type of action is this request for?
     * Examples: "CREATE_USER", "MODIFY_USER", "INITIATE_PAYMENT", "ADD_BENEFICIARY"
     *
     * This helps checkers quickly understand what they're approving.
     */
    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType;

    /**
     * The data payload of the request stored as JSON string.
     * Contains all the details needed to execute the action upon approval.
     *
     * For CREATE_USER: Contains user details (name, email, roles, etc.)
     * For INITIATE_PAYMENT: Contains payment details (amount, beneficiary, etc.)
     *
     * Using @Lob to store large JSON payloads (up to 2GB in PostgreSQL).
     */
    @Lob
    @Column(name = "request_payload", nullable = false, columnDefinition = "TEXT")
    private String requestPayload;

    // ---- Maker (Creator) Information ----

    /**
     * UUID of the user who CREATED this request (the Maker).
     * The maker cannot approve their own request (enforced in service layer).
     */
    @Column(name = "maker_id", nullable = false)
    private UUID makerId;

    /** Name of the maker — stored for audit trail even if maker account is deleted */
    @Column(name = "maker_name", length = 150)
    private String makerName;

    /** When the maker submitted this request */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ---- Checker (Approver) Information ----

    /**
     * UUID of the CHECKER who acted on this request.
     * Null until a checker approves or rejects.
     */
    @Column(name = "checker_id")
    private UUID checkerId;

    /** Name of the checker — stored for audit trail */
    @Column(name = "checker_name", length = 150)
    private String checkerName;

    /** When the checker made their decision */
    @Column(name = "actioned_at")
    private LocalDateTime actionedAt;

    // ---- Approval Status ----

    /**
     * Current status of this approval request.
     * Default: PENDING — waiting for checker action.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ApprovalStatus status = ApprovalStatus.PENDING;

    /**
     * Reason provided by the checker when REJECTING the request.
     * Mandatory on rejection. Helps maker understand what to fix.
     */
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    /**
     * Optional comments from the checker (even on approvals).
     * Example: "Approved. Verify with HR for role assignment."
     */
    @Column(name = "checker_comments", length = 500)
    private String checkerComments;

    // ---- Reference to Affected Entity ----

    /**
     * ID of the entity this request is about.
     * For user requests: the User's UUID.
     * For payment requests: the Transaction's UUID.
     */
    @Column(name = "entity_id")
    private UUID entityId;

    /**
     * Type of entity this request affects.
     * Examples: "USER", "TRANSACTION", "BENEFICIARY"
     */
    @Column(name = "entity_type", length = 30)
    private String entityType;

    /** Priority level: "LOW", "MEDIUM", "HIGH", "CRITICAL" */
    @Column(name = "priority", length = 10)
    @Builder.Default
    private String priority = "MEDIUM";

    /** Expiry time — request auto-expires if not actioned (e.g., 48 hours) */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        // Default expiry: 48 hours from creation
        if (this.expiresAt == null) {
            this.expiresAt = LocalDateTime.now().plusHours(48);
        }
    }
}
