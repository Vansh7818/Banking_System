package com.kyrodatatech.banking.domain.user.repository;

import com.kyrodatatech.banking.domain.user.entity.User;
import com.kyrodatatech.banking.domain.user.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ================================================================
 * UserRepository — Database Access for User Entity
 * ================================================================
 *
 * JpaRepository provides standard CRUD methods out of the box:
 *   - save(user)             → INSERT or UPDATE
 *   - findById(id)           → SELECT by primary key
 *   - findAll()              → SELECT all users
 *   - deleteById(id)         → DELETE by primary key
 *   - existsById(id)         → Returns boolean
 *   - count()                → Returns total count
 *
 * We ADD custom query methods below using Spring Data's "method naming" feature.
 * Spring automatically generates the SQL from the method name!
 *
 * Example: findByEmail(email) → SELECT * FROM users WHERE email = ?
 *
 * @Repository — Marks this as a Spring Data repository
 * JpaRepository<User, UUID> — Works with User entity, UUID primary key
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find a user by their email address.
     * Used by CustomUserDetailsService for authentication.
     *
     * Spring generates: SELECT * FROM users WHERE email = ?
     *
     * @param email The email to search for
     * @return Optional<User> — empty if not found, contains User if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if an email address already exists in the database.
     * Used during registration to prevent duplicate accounts.
     *
     * Spring generates: SELECT COUNT(*) > 0 FROM users WHERE email = ?
     *
     * @param email The email to check
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find all users with a specific status.
     * Useful for admins to see all PENDING_APPROVAL accounts.
     *
     * @param status The user status to filter by
     * @return List of users with that status
     */
    List<User> findByStatus(UserStatus status);

    /**
     * Find a user by their employee ID.
     * Used for HR system integration and internal lookups.
     *
     * @param employeeId The employee ID to search for
     * @return Optional<User>
     */
    Optional<User> findByEmployeeId(String employeeId);

    /**
     * Custom JPQL query to find all users in a specific corporate group.
     * JPQL queries reference Java class/field names, not SQL table/column names.
     *
     * @param corporateGroupId UUID of the corporate group
     * @return List of users belonging to that corporate group
     */
    @Query("SELECT u FROM User u WHERE u.corporateGroup.id = :corporateGroupId")
    List<User> findByCorporateGroupId(UUID corporateGroupId);

    /**
     * Find all users belonging to a specific legal entity.
     *
     * @param legalEntityId UUID of the legal entity
     * @return List of users in that legal entity
     */
    @Query("SELECT u FROM User u WHERE u.legalEntity.id = :legalEntityId")
    List<User> findByLegalEntityId(UUID legalEntityId);
}
