package com.kyrodatatech.banking;

import com.kyrodatatech.banking.domain.user.entity.Role;
import com.kyrodatatech.banking.domain.user.entity.User;
import com.kyrodatatech.banking.domain.user.enums.RoleType;
import com.kyrodatatech.banking.domain.user.enums.UserStatus;
import com.kyrodatatech.banking.domain.user.repository.RoleRepository;
import com.kyrodatatech.banking.domain.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

/**
 * ================================================================
 * CMS Finance & Banking System — Application Entry Point
 * ================================================================
 *
 * On startup, creates one demo user per role so any team member
 * can log in and explore the system. All passwords: "password123"
 *
 * Access Swagger UI: http://localhost:8080/swagger-ui.html
 * Frontend Dashboard: http://localhost:3000
 *
 * @author  KyroDataTech Engineering Team
 * @version 1.0.0
 */
@SpringBootApplication
public class BankingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankingSystemApplication.class, args);
    }

    @Bean
    public CommandLineRunner seedDemoUsers(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            // Define the users to seed: email, fullName, role
            Object[][] users = {
                {"admin@kyrobank.com",       "Super Admin",           RoleType.BANK_SUPER_ADMIN},
                {"ops@kyrobank.com",         "Payment Ops Manager",   RoleType.PAYMENT_OPERATIONS},
                {"compliance@kyrobank.com",  "Compliance Officer",    RoleType.COMPLIANCE_OFFICER},
                {"aml@kyrobank.com",         "AML Reviewer",          RoleType.AML_SANCTIONS_REVIEWER},
                {"maker@kyrobank.com",       "Corporate Maker",       RoleType.CORP_MAKER},
                {"checker@kyrobank.com",     "Corporate Checker",     RoleType.CORP_CHECKER},
                {"approver@kyrobank.com",    "Corporate Approver",    RoleType.CORP_APPROVER_L1},
                {"corpadmin@kyrobank.com",   "Corporate Admin",       RoleType.CORP_ADMIN},
            };

            for (Object[] u : users) {
                String email    = (String) u[0];
                String name     = (String) u[1];
                RoleType roleType = (RoleType) u[2];

                if (userRepository.findByEmail(email).isEmpty()) {
                    Role role = roleRepository.findByRoleType(roleType)
                        .orElseGet(() -> {
                            Role r = new Role();
                            r.setRoleType(roleType);
                            return roleRepository.save(r);
                        });

                    User user = new User();
                    user.setEmail(email);
                    user.setPassword(passwordEncoder.encode("password123"));
                    user.setFullName(name);
                    user.setRoles(Set.of(role));
                    user.setStatus(UserStatus.ACTIVE);
                    userRepository.save(user);
                    System.out.println("✅ SEEDED: " + email + " [" + roleType + "]");
                }
            }
        };
    }
}
