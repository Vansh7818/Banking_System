package com.kyrodatatech.banking.domain.user.repository;

import com.kyrodatatech.banking.domain.user.entity.MakerCheckerRequest;
import com.kyrodatatech.banking.domain.user.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * ================================================================
 * MakerCheckerRepository — Database Access for Approval Requests
 * ================================================================
 *
 * Provides CRUD operations for the 'maker_checker_requests' table.
 *
 * COMMON USAGE:
 *   // Checker views pending requests:
 *   List<MakerCheckerRequest> pending = repo.findByStatus(ApprovalStatus.PENDING);
 *
 *   // View requests created by a specific maker:
 *   List<MakerCheckerRequest> myRequests = repo.findByMakerId(makerId);
 */
@Repository
public interface MakerCheckerRepository extends JpaRepository<MakerCheckerRequest, UUID> {

    /**
     * Find all approval requests with a specific status.
     * Used by checkers to see all PENDING requests that need their action.
     *
     * @param status ApprovalStatus (PENDING, APPROVED, REJECTED, CANCELLED)
     * @return List of matching requests
     */
    List<MakerCheckerRequest> findByStatus(ApprovalStatus status);

    /**
     * Find all requests submitted by a specific maker.
     * Used by makers to see the status of their submitted requests.
     *
     * @param makerId UUID of the maker user
     * @return List of requests created by this maker
     */
    List<MakerCheckerRequest> findByMakerId(UUID makerId);

    /**
     * Find all requests actioned by a specific checker.
     * Useful for audit trails — "what did this checker approve/reject?"
     *
     * @param checkerId UUID of the checker user
     * @return List of requests actioned by this checker
     */
    List<MakerCheckerRequest> findByCheckerId(UUID checkerId);

    /**
     * Find all requests related to a specific entity.
     * Example: All requests related to user UUID "abc-123"
     *
     * @param entityId   UUID of the entity (user, transaction, etc.)
     * @param entityType Type of entity ("USER", "TRANSACTION", etc.)
     * @return List of matching requests
     */
    List<MakerCheckerRequest> findByEntityIdAndEntityType(UUID entityId, String entityType);

    /**
     * Find PENDING requests for a specific action type.
     * Example: All pending CREATE_USER requests.
     *
     * @param actionType  The action type (e.g., "CREATE_USER")
     * @param status      The status (e.g., PENDING)
     * @return List of matching requests
     */
    List<MakerCheckerRequest> findByActionTypeAndStatus(String actionType, ApprovalStatus status);
}
