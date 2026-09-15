package com.kyrodatatech.banking.domain.user.repository;

import com.kyrodatatech.banking.domain.user.entity.Role;
import com.kyrodatatech.banking.domain.user.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ================================================================
 * RoleRepository — Database Access for Role Entity
 * ================================================================
 *
 * Provides CRUD operations for the 'roles' table.
 * Roles are pre-seeded into the database when the application starts
 * (via DataInitializer or Flyway migration scripts).
 *
 * USAGE:
 *   When creating a new user, we look up the Role by RoleType:
 *
 *   Role makerRole = roleRepository.findByRoleType(RoleType.CORP_MAKER)
 *       .orElseThrow(() -> new AppException("Role not found", HttpStatus.INTERNAL_SERVER_ERROR));
 *   user.getRoles().add(makerRole);
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Find a role by its type.
     * Used when assigning roles to users.
     *
     * @param roleType The RoleType enum value
     * @return Optional<Role> — the Role entity for this type
     */
    Optional<Role> findByRoleType(RoleType roleType);

    /**
     * Check if a role type already exists in the database.
     * Used during application startup to avoid duplicate role seeding.
     *
     * @param roleType The RoleType to check
     * @return true if role exists
     */
    boolean existsByRoleType(RoleType roleType);
}
