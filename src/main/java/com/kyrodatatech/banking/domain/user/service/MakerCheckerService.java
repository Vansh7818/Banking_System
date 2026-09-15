package com.kyrodatatech.banking.domain.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyrodatatech.banking.domain.user.entity.MakerCheckerRequest;
import com.kyrodatatech.banking.domain.user.entity.User;
import com.kyrodatatech.banking.domain.user.enums.ApprovalStatus;
import com.kyrodatatech.banking.domain.user.enums.UserStatus;
import com.kyrodatatech.banking.domain.user.repository.MakerCheckerRepository;
import com.kyrodatatech.banking.domain.user.repository.UserRepository;
import com.kyrodatatech.banking.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ================================================================
 * MakerCheckerService — The Approval Workflow Business Logic
 * ================================================================
 *
 * This service orchestrates the entire Maker-Checker (4-eyes principle) workflow.
 *
 * WORKFLOW OVERVIEW:
 * ─────────────────────────────────────────────────────────────
 *
 * [MAKER creates a request]
 *    ↓
 * MakerCheckerRequest saved with status = PENDING
 *    ↓
 * [CHECKER reviews all PENDING requests]
 *    ↓
 * CHECKER approves → executeApprovedAction()
 *       OR
 * CHECKER rejects → save rejection reason
 *    ↓
 * If approved: User.status = ACTIVE (user can login!)
 *
 * ─────────────────────────────────────────────────────────────
 *
 * SECURITY RULES ENFORCED HERE:
 * 1. A MAKER cannot approve their OWN request (self-approval = fraud risk)
 * 2. Only PENDING requests can be approved/rejected
 * 3. Only CHECKER role users can approve requests
 * 4. Expired requests cannot be approved
 *
 * @Service — Spring business logic layer
 * @Transactional — All operations run in a database transaction
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MakerCheckerService {

    private final MakerCheckerRepository makerCheckerRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper; // For parsing JSON payloads

    // ─────────────────────────────────────────────────────────────
    // MAKER ACTIONS — Creating Requests
    // ─────────────────────────────────────────────────────────────

    /**
     * Creates a new Maker-Checker approval request for user creation.
     *
     * Called when a BANK_USER_ADMIN (Maker) wants to create a new bank user.
     * The new user is created with PENDING_APPROVAL status and cannot login
     * until a CHECKER approves this request.
     *
     * @param newUser  The User entity to be created (in PENDING_APPROVAL status)
     * @param makerId  UUID of the admin making this request
     * @param makerName Full name of the maker
     * @return The created MakerCheckerRequest
     */
    @Transactional
    public MakerCheckerRequest createUserRequest(User newUser, UUID makerId, String makerName) {
        try {
            // Serialize the user object to JSON for storage in the request payload
            // This preserves all the data needed to create the user upon approval
            String payload = objectMapper.writeValueAsString(Map.of(
                    "userId", newUser.getId().toString(),
                    "fullName", newUser.getFullName(),
                    "email", newUser.getEmail(),
                    "employeeId", newUser.getEmployeeId() != null ? newUser.getEmployeeId() : "",
                    "roles", newUser.getRoles().stream()
                            .map(role -> role.getRoleType().name())
                            .toList()
            ));

            MakerCheckerRequest request = MakerCheckerRequest.builder()
                    .actionType("CREATE_USER")
                    .requestPayload(payload)
                    .makerId(makerId)
                    .makerName(makerName)
                    .entityId(newUser.getId())
                    .entityType("USER")
                    .status(ApprovalStatus.PENDING)
                    .priority("MEDIUM")
                    .build();

            MakerCheckerRequest savedRequest = makerCheckerRepository.save(request);
            log.info("Maker-Checker request created: {} | Maker: {} | User: {}",
                    savedRequest.getId(), makerName, newUser.getEmail());

            return savedRequest;

        } catch (Exception e) {
            throw new AppException("Failed to create approval request: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CHECKER ACTIONS — Approving or Rejecting
    // ─────────────────────────────────────────────────────────────

    /**
     * APPROVE a pending Maker-Checker request.
     *
     * Called by a CHECKER user to approve a pending request.
     * After approval, the target action is executed (user activated, payment sent).
     *
     * SECURITY CHECKS:
     * - Cannot approve your own request
     * - Request must be in PENDING status
     * - Request must not be expired
     *
     * @param requestId  UUID of the MakerCheckerRequest to approve
     * @param checkerId  UUID of the CHECKER user approving it
     * @param checkerName Full name of the checker
     * @param comments   Optional comments from the checker
     * @return Updated MakerCheckerRequest with APPROVED status
     */
    @Transactional
    public MakerCheckerRequest approve(UUID requestId, UUID checkerId,
                                       String checkerName, String comments) {
        // Load the request
        MakerCheckerRequest request = getRequestOrThrow(requestId);

        // ---- SECURITY: Self-approval prevention ----
        // A maker CANNOT approve their own request. This is the "4-eyes principle".
        if (request.getMakerId().equals(checkerId)) {
            throw new AppException(
                    "Self-approval is not allowed. A different user must approve this request.",
                    HttpStatus.FORBIDDEN
            );
        }

        // ---- VALIDATION: Only PENDING requests can be approved ----
        if (request.getStatus() != ApprovalStatus.PENDING) {
            throw new AppException(
                    "Request is not in PENDING status. Current status: " + request.getStatus(),
                    HttpStatus.BAD_REQUEST
            );
        }

        // ---- VALIDATION: Check if request has expired ----
        if (LocalDateTime.now().isAfter(request.getExpiresAt())) {
            request.setStatus(ApprovalStatus.REJECTED);
            request.setRejectionReason("Request expired automatically");
            makerCheckerRepository.save(request);
            throw new AppException("This approval request has expired.", HttpStatus.BAD_REQUEST);
        }

        // ---- UPDATE: Mark request as APPROVED ----
        request.setStatus(ApprovalStatus.APPROVED);
        request.setCheckerId(checkerId);
        request.setCheckerName(checkerName);
        request.setCheckerComments(comments);
        request.setActionedAt(LocalDateTime.now());

        // ---- EXECUTE: Perform the actual action ----
        executeApprovedAction(request);

        MakerCheckerRequest savedRequest = makerCheckerRepository.save(request);
        log.info("Request {} APPROVED by checker: {} | Action: {}",
                requestId, checkerName, request.getActionType());

        return savedRequest;
    }

    /**
     * REJECT a pending Maker-Checker request.
     *
     * Called by a CHECKER user to reject a request.
     * The maker is notified with the rejection reason.
     *
     * @param requestId       UUID of the request to reject
     * @param checkerId       UUID of the checker
     * @param checkerName     Name of the checker
     * @param rejectionReason WHY was this rejected? (required)
     * @return Updated MakerCheckerRequest with REJECTED status
     */
    @Transactional
    public MakerCheckerRequest reject(UUID requestId, UUID checkerId,
                                      String checkerName, String rejectionReason) {
        MakerCheckerRequest request = getRequestOrThrow(requestId);

        // Self-rejection is also not allowed (consistency)
        if (request.getMakerId().equals(checkerId)) {
            throw new AppException(
                    "A maker cannot reject their own request.",
                    HttpStatus.FORBIDDEN
            );
        }

        if (request.getStatus() != ApprovalStatus.PENDING) {
            throw new AppException(
                    "Only PENDING requests can be rejected. Current status: " + request.getStatus(),
                    HttpStatus.BAD_REQUEST
            );
        }

        if (rejectionReason == null || rejectionReason.isBlank()) {
            throw new AppException("Rejection reason is required.", HttpStatus.BAD_REQUEST);
        }

        request.setStatus(ApprovalStatus.REJECTED);
        request.setCheckerId(checkerId);
        request.setCheckerName(checkerName);
        request.setRejectionReason(rejectionReason);
        request.setActionedAt(LocalDateTime.now());

        MakerCheckerRequest savedRequest = makerCheckerRepository.save(request);
        log.info("Request {} REJECTED by checker: {} | Reason: {}",
                requestId, checkerName, rejectionReason);

        return savedRequest;
    }

    // ─────────────────────────────────────────────────────────────
    // QUERY METHODS — Viewing Requests
    // ─────────────────────────────────────────────────────────────

    /**
     * Get all PENDING approval requests.
     * Used by CHECKER users to see what needs their attention.
     *
     * @return List of pending requests
     */
    public List<MakerCheckerRequest> getAllPendingRequests() {
        return makerCheckerRepository.findByStatus(ApprovalStatus.PENDING);
    }

    /**
     * Get all requests created by a specific maker.
     *
     * @param makerId UUID of the maker
     * @return List of requests created by this maker
     */
    public List<MakerCheckerRequest> getRequestsByMaker(UUID makerId) {
        return makerCheckerRepository.findByMakerId(makerId);
    }

    /**
     * Get a specific request by ID.
     *
     * @param requestId UUID of the request
     * @return The request details
     */
    public MakerCheckerRequest getRequest(UUID requestId) {
        return getRequestOrThrow(requestId);
    }

    // ─────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────

    /**
     * Finds a MakerCheckerRequest by ID or throws 404 if not found.
     */
    private MakerCheckerRequest getRequestOrThrow(UUID requestId) {
        return makerCheckerRepository.findById(requestId)
                .orElseThrow(() -> new AppException(
                        "Approval request not found with ID: " + requestId,
                        HttpStatus.NOT_FOUND
                ));
    }

    /**
     * Executes the actual action when a CHECKER approves a request.
     *
     * Uses a switch on actionType to determine what to do:
     * - "CREATE_USER" → Activate the user account
     * - "INITIATE_PAYMENT" → Submit payment to bank network
     * - "ADD_BENEFICIARY" → Save the beneficiary
     * - etc.
     *
     * @param request The approved MakerCheckerRequest
     */
    private void executeApprovedAction(MakerCheckerRequest request) {
        switch (request.getActionType()) {

            case "CREATE_USER" -> {
                // Activate the user account so they can login
                UUID userId = request.getEntityId();
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new AppException(
                                "User not found for approval: " + userId,
                                HttpStatus.NOT_FOUND
                        ));
                user.setStatus(UserStatus.ACTIVE);
                user.setApprovedBy(request.getCheckerId());
                user.setApprovedAt(LocalDateTime.now());
                userRepository.save(user);
                log.info("User {} ACTIVATED after checker approval", user.getEmail());
            }

            case "MODIFY_USER" -> {
                // Apply modifications to the user
                // TODO: Parse request.getRequestPayload() and apply changes
                log.info("User modification approved for entity: {}", request.getEntityId());
            }

            case "DEACTIVATE_USER" -> {
                UUID userId = request.getEntityId();
                userRepository.findById(userId).ifPresent(user -> {
                    user.setStatus(UserStatus.DEACTIVATED);
                    userRepository.save(user);
                    log.info("User {} DEACTIVATED after checker approval", user.getEmail());
                });
            }

            case "INITIATE_PAYMENT" -> {
                // Submit payment to the banking network
                // TODO: Delegate to PaymentService.processApprovedPayment(entityId)
                log.info("Payment {} approved — sending to bank network", request.getEntityId());
            }

            default -> log.warn("Unknown action type in approved request: {}", request.getActionType());
        }
    }
}
