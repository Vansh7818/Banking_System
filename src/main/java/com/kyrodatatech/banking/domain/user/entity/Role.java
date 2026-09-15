package com.kyrodatatech.banking.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import com.kyrodatatech.banking.domain.user.enums.RoleType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * ================================================================
 * Role Entity — Maps to Database 'roles' Table
 * ================================================================
 *
 * A Role represents a specific job function in the banking system.
 * Users are assigned one or more roles that determine what they can do.
 *
 * DATABASE TABLE: 'roles'
 *
 * EXAMPLE ROWS:
 *   id | role_type            | description
 *   ---+----------------------+--------------------------------
 *   1  | BANK_SUPER_ADMIN     | Full system access
 *   2  | CORP_MAKER           | Creates transactions for approval
 *   3  | AML_SANCTIONS_REVIEWER | Reviews flagged transactions
 *
 * HOW THIS RELATES TO USERS:
 *   - Many-to-Many relationship via 'user_roles' table
 *   - One user can have multiple roles
 *   - Many users can share the same role
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    /**
     * Auto-generated primary key (Long/integer ID is fine for roles
     * since the list is small and predictable, unlike users).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * The role type — links to our RoleType enum.
     * @Enumerated(STRING) — Stored as text "BANK_SUPER_ADMIN" not number 1
     * (Using STRING makes the database readable and prevents bugs if enum order changes)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false, unique = true, length = 60)
    private RoleType roleType;

    /**
     * Human-readable description of what this role can do.
     * Used in the admin UI to explain roles to bank staff.
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * The category of this role for grouping in UI:
     * "BANK_ADMIN", "BANK_OPERATIONS", "BANK_RISK", "CORPORATE"
     */
    @Column(name = "category", length = 50)
    private String category;

    /**
     * Back-reference to see which users have this role.
     * 'mappedBy = "roles"' means the 'users' side owns the join table.
     * We typically don't use this direction, but it's good practice to map both sides.
     */
    @ManyToMany(mappedBy = "roles")
    @Builder.Default
    private Set<User> users = new HashSet<>();
}
