package com.kyrodatatech.banking.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ================================================================
 * LegalEntity — A Subsidiary or Branch of a CorporateGroup
 * ================================================================
 *
 * A Legal Entity is a specific company within a corporate group
 * that can independently hold accounts and initiate transactions.
 *
 * EXAMPLE:
 *   CorporateGroup: "Tata Group"
 *     ├── LegalEntity: "Tata Consultancy Services Ltd" (CIN: L22210MH1995PLC084781)
 *     ├── LegalEntity: "Tata Motors Ltd"
 *     └── LegalEntity: "Tata Steel Ltd"
 *
 * Each legal entity can have its own:
 *   - Bank accounts
 *   - Users (Corporate Admin, Makers, Checkers)
 *   - Payment limits and workflows
 *
 * DATABASE TABLE: 'legal_entities'
 */
@Entity
@Table(name = "legal_entities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LegalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * The parent corporate group this entity belongs to.
     * @ManyToOne — Many legal entities can belong to one corporate group.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corporate_group_id", nullable = false)
    private CorporateGroup corporateGroup;

    /**
     * Official registered name of this legal entity.
     * Example: "Tata Consultancy Services Limited"
     */
    @Column(name = "entity_name", nullable = false, length = 200)
    private String entityName;

    /**
     * Short code for this entity. Used in reference numbers.
     * Example: "TCSL", "TATAMOTORS"
     */
    @Column(name = "entity_code", nullable = false, length = 30)
    private String entityCode;

    /**
     * Company Identification Number (CIN) / Registration Number.
     * Example: "L22210MH1995PLC084781"
     */
    @Column(name = "registration_number", length = 50)
    private String registrationNumber;

    /**
     * GST Identification Number — required for Indian corporates.
     * Example: "27AABCT3518Q1ZD"
     */
    @Column(name = "gstin", length = 20)
    private String gstin;

    /**
     * Country where this legal entity is registered.
     * ISO 3166-1 alpha-2 code.
     */
    @Column(name = "country_code", length = 3)
    private String countryCode;

    /** Is this entity currently active on the platform? */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Users belonging to this legal entity.
     * @OneToMany — One legal entity can have many corporate users.
     */
    @OneToMany(mappedBy = "legalEntity", fetch = FetchType.LAZY)
    @Builder.Default
    private List<User> users = new ArrayList<>();

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
