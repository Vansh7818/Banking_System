package com.kyrodatatech.banking.domain.user.enums;

/**
 * ================================================================
 * UserStatus — Lifecycle status of any User in the system
 * ================================================================
 *
 * A user goes through these states from creation to deactivation.
 * Only ACTIVE users can log in and perform actions.
 *
 * Lifecycle flow:
 *   PENDING_APPROVAL → ACTIVE → SUSPENDED / DEACTIVATED
 */
public enum UserStatus {

    /**
     * User has been created by an Admin MAKER but not yet
     * approved by an Admin CHECKER. Cannot log in.
     */
    PENDING_APPROVAL,

    /**
     * User is fully active and can log in.
     * Status set by Admin CHECKER upon approval.
     */
    ACTIVE,

    /**
     * User account temporarily suspended (e.g., policy violation, security risk).
     * Can be re-activated. Cannot log in.
     */
    SUSPENDED,

    /**
     * User account permanently deactivated.
     * Cannot be re-activated. Cannot log in.
     */
    DEACTIVATED,

    /**
     * User was invited but hasn't set their password yet.
     */
    INVITED
}
