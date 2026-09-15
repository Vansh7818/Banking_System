package com.kyrodatatech.banking.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ================================================================
 * CorporateGroup Entity — Top-Level Corporate Client Container
 * ================================================================
 *
 * A CorporateGroup represents a corporate client at the highest level.
 * Examples: "Tata Group", "Reliance Industries", "Infosys Ltd."
 *
 * HIERARCHY:
 *   CorporateGroup
 *       └── LegalEntity (one or more)
 *               └── Users (corporate staff)
 *
 * DATABASE TABLE: 'corporate_groups'
 *
 * A corporate group can have multiple Legal Entities (subsidiaries, branches),
 * and each Legal Entity has its own set of users.
 */
@Entity
@Table(name = "corporate_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorporateGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Official name of the corporate group.
     * Example: "Tata Consultancy Services Group"
     */
    @Column(name = "group_name", nullable = false, length = 200)
    private String groupName;

    /**
     * Short code for the group — used in reference numbers and reports.
     * Example: "TCS", "RIL", "INFY"
     */
    @Column(name = "group_code", unique = true, nullable = false, length = 20)
    private String groupCode;

    /**
     * Country where the corporate group is headquartered.
     * ISO 3166-1 alpha-2 code. Example: "IN", "US", "GB"
     */
    @Column(name = "country_code", length = 3)
    private String countryCode;

    /**
     * Industry sector of the corporate group.
     * Example: "IT", "MANUFACTURING", "FINANCIAL_SERVICES"
     */
    @Column(name = "industry_sector", length = 100)
    private String industrySector;

    /** Is this corporate group currently active? */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * All Legal Entities (subsidiaries/branches) that belong to this group.
     * @OneToMany — One group can have many legal entities
     * mappedBy = "corporateGroup" — The foreign key is on the LegalEntity side
     * cascade = ALL — If group is deleted, delete all its legal entities too
     */
    @OneToMany(mappedBy = "corporateGroup", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LegalEntity> legalEntities = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
