package com.kyrodatatech.banking.domain.user.enums;

/**
 * ================================================================
 * ApprovalStatus — Status of a Maker-Checker Approval Request
 * ================================================================
 *
 * In the Maker-Checker workflow, every important action (creating a user,
 * initiating a high-value payment) must be reviewed by a second person.
 *
 * The lifecycle is:
 *   PENDING → APPROVED (user activated / payment processed)
 *   PENDING → REJECTED (user creation cancelled / payment rejected)
 *   PENDING → CANCELLED (maker themselves cancels)
 */
public enum ApprovalStatus {

    /**
     * The request has been created by the MAKER and is waiting
     * for a CHECKER to review it. Default initial state.
     */
    PENDING,

    /**
     * The CHECKER has reviewed and approved the request.
     * The target action will now be executed (user activated, payment sent).
     */
    APPROVED,

    /**
     * The CHECKER has reviewed and rejected the request.
     * The maker will be notified with the rejection reason.
     */
    REJECTED,

    /**
     * The MAKER has cancelled the request before a CHECKER acted on it.
     * Only the original maker can cancel.
     */
    CANCELLED
}
