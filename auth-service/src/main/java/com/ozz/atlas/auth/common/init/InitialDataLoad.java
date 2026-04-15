package com.ozz.atlas.auth.common.init;

import com.ozz.atlas.auth.domain.*;
import com.ozz.atlas.auth.repository.OrganizationRepository;
import com.ozz.atlas.auth.repository.UserRepository;
import com.ozz.atlas.common.jpa.Status;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class InitialDataLoad implements CommandLineRunner {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public InitialDataLoad(OrganizationRepository organizationRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByLoginId("admin").isPresent()) {
            return;
        }

        Organization adminOrganization = organizationRepository.save(
                Organization.builder()
                        .organizationType(OrganizationType.ADMIN)
                        .organizationName("아틀라스 관리조직")
                        .businessNo(null)
                        .contactFirstName("시스템")
                        .contactLastName("관리자")
                        .contactEmail("admin@atlas.com")
                        .contactPhone("010-9999-9999")
                        .status(Status.ACTIVE)
                        .build()
        );

        userRepository.save(
                User.builder()
                        .organization(adminOrganization)
                        .loginId("admin")
                        .password(passwordEncoder.encode("12341234"))
                        .firstName("시스템")
                        .lastName("관리자")
                        .email("admin@atlas.com")
                        .phone("010-9999-9999")
                        .jobTitle("시스템관리자")
                        .userRole(UserRole.ADMIN)
                        .status(Status.ACTIVE)
                        .build()
        );

        createTestUserIfAbsent(
                "user1",
                "user1@atlas.com",
                "Buyer",
                "Admin",
                "010-1111-1111",
                "Buyer Manager",
                UserRole.ORG_ADMIN,
                OrganizationType.BUYER,
                null,
                "Atlas Buyer Org",
                "111-11-11111",
                "Buyer",
                "Manager",
                "buyer-org@atlas.com",
                "010-1111-0000"
        );

        createTestUserIfAbsent(
                "user2",
                "user2@atlas.com",
                "Tier1",
                "Admin",
                "010-2222-2222",
                "Tier1 Supplier Manager",
                UserRole.ORG_ADMIN,
                OrganizationType.SUPPLIER,
                1,
                "Atlas Supplier Tier1",
                "222-22-22222",
                "Tier1",
                "Manager",
                "tier1-org@atlas.com",
                "010-2222-0000"
        );

        createTestUserIfAbsent(
                "user3",
                "user3@atlas.com",
                "Tier2",
                "Admin",
                "010-3333-3333",
                "Tier2 Supplier Manager",
                UserRole.ORG_ADMIN,
                OrganizationType.SUPPLIER,
                2,
                "Atlas Supplier Tier2",
                "333-33-33333",
                "Tier2",
                "Manager",
                "tier2-org@atlas.com",
                "010-3333-0000"
        );

        createTestUserIfAbsent(
                "user4",
                "user4@atlas.com",
                "Tier3",
                "Admin",
                "010-4444-4444",
                "Tier3 Supplier Manager",
                UserRole.ORG_ADMIN,
                OrganizationType.SUPPLIER,
                3,
                "Atlas Supplier Tier3",
                "444-44-44444",
                "Tier3",
                "Manager",
                "tier3-org@atlas.com",
                "010-4444-0000"
        );


    }

    private void createTestUserIfAbsent(
            String loginId,
            String email,
            String firstName,
            String lastName,
            String phone,
            String jobTitle,
            UserRole userRole,
            OrganizationType organizationType,
            Integer tierLevel,
            String organizationName,
            String businessNo,
            String contactFirstName,
            String contactLastName,
            String contactEmail,
            String contactPhone
    ) {
        if (userRepository.existsByLoginId(loginId)) {
            return;
        }

        Organization organization = organizationRepository.save(
                Organization.builder()
                        .organizationType(organizationType)
                        .organizationName(organizationName)
                        .businessNo(businessNo)
                        .contactFirstName(contactFirstName)
                        .contactLastName(contactLastName)
                        .contactEmail(contactEmail)
                        .contactPhone(contactPhone)
                        .status(Status.ACTIVE)
                        .tierLevel(tierLevel)
                        .build()
        );

        userRepository.save(
                User.builder()
                        .organization(organization)
                        .loginId(loginId)
                        .password(passwordEncoder.encode("12341234"))
                        .firstName(firstName)
                        .lastName(lastName)
                        .email(email)
                        .phone(phone)
                        .jobTitle(jobTitle)
                        .userRole(userRole)
                        .status(Status.ACTIVE)
                        .build()
        );
    }



}
