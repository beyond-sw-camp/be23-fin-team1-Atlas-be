package com.ozz.atlas.auth.common.init;

import com.ozz.atlas.auth.domain.Department;
import com.ozz.atlas.auth.domain.Organization;
import com.ozz.atlas.auth.domain.OrganizationType;
import com.ozz.atlas.auth.domain.User;
import com.ozz.atlas.auth.domain.UserRole;
import com.ozz.atlas.auth.repository.DepartmentRepository;
import com.ozz.atlas.auth.repository.OrganizationRepository;
import com.ozz.atlas.auth.repository.UserRepository;
import com.ozz.atlas.common.jpa.Status;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class InitialDataLoad implements CommandLineRunner {

    private static final String DEFAULT_PASSWORD = "12341234";

    private final OrganizationRepository organizationRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public InitialDataLoad(OrganizationRepository organizationRepository,
                           DepartmentRepository departmentRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.organizationRepository = organizationRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        createDepartmentIfAbsent("LOGISTICS_DEPARTMENT", "물류 부서");
        createDepartmentIfAbsent("QUALITY_DEPARTMENT", "품질 부서");
        createDepartmentIfAbsent("PURCHASE_DEPARTMENT", "구매 부서");

        SeedOrganization adminOrganization = new SeedOrganization(
                OrganizationType.ADMIN,
                "아틀라스 관리조직",
                "Atlas Platform Admin",
                "ATLAS",
                null,
                "시스템",
                null,
                "관리자",
                "admin@atlas.com",
                "010-9999-9999"
        );

        createSeedUsers(adminOrganization, List.of(
                new SeedUser(
                        "admin",
                        "admin@atlas.com",
                        "시스템",
                        null,
                        "관리자",
                        "010-9999-9999",
                        "플랫폼 관리자",
                        UserRole.ADMIN
                ),
                new SeedUser(
                        "admin2",
                        "admin2@atlas.com",
                        "플랫폼",
                        null,
                        "관리자",
                        "010-9999-9998",
                        "플랫폼 관리자",
                        UserRole.ADMIN
                )
        ));

        createSeedUser(
                new SeedOrganization(
                        OrganizationType.BUYER,
                        "Atlas Buyer Org",
                        "Atlas Buyer Organization",
                        "BUYER",
                        "111-11-11111",
                        "Buyer",
                        null,
                        "Manager",
                        "buyer-org@atlas.com",
                        "010-1111-0000"
                ),
                new SeedUser(
                        "user1",
                        "user1@atlas.com",
                        "Buyer",
                        null,
                        "Admin",
                        "010-1111-1111",
                        "Buyer Manager",
                        UserRole.ORG_ADMIN
                )
        );

        createSeedUser(
                new SeedOrganization(
                        OrganizationType.SUPPLIER,
                        "Atlas Supplier Tier1",
                        "Atlas Supplier Tier1",
                        "TIER1",
                        "222-22-22222",
                        "Tier1",
                        null,
                        "Manager",
                        "tier1-org@atlas.com",
                        "010-2222-0000"
                ),
                new SeedUser(
                        "user2",
                        "user2@atlas.com",
                        "Tier1",
                        null,
                        "Admin",
                        "010-2222-2222",
                        "Tier1 Supplier Manager",
                        UserRole.ORG_ADMIN
                )
        );

        createSeedUser(
                new SeedOrganization(
                        OrganizationType.SUPPLIER,
                        "Atlas Supplier Tier2",
                        "Atlas Supplier Tier2",
                        "TIER2",
                        "333-33-33333",
                        "Tier2",
                        null,
                        "Manager",
                        "tier2-org@atlas.com",
                        "010-3333-0000"
                ),
                new SeedUser(
                        "user3",
                        "user3@atlas.com",
                        "Tier2",
                        null,
                        "Admin",
                        "010-3333-3333",
                        "Tier2 Supplier Manager",
                        UserRole.ORG_ADMIN
                )
        );

        createSeedUser(
                new SeedOrganization(
                        OrganizationType.SUPPLIER,
                        "Atlas Supplier Tier3",
                        "Atlas Supplier Tier3",
                        "TIER3",
                        "444-44-44444",
                        "Tier3",
                        null,
                        "Manager",
                        "tier3-org@atlas.com",
                        "010-4444-0000"
                ),
                new SeedUser(
                        "user4",
                        "user4@atlas.com",
                        "Tier3",
                        null,
                        "Admin",
                        "010-4444-4444",
                        "Tier3 Supplier Manager",
                        UserRole.ORG_ADMIN
                )
        );
    }

    private void createDepartmentIfAbsent(String departmentCode, String departmentName) {
        if (departmentRepository.existsByDepartmentCode(departmentCode)) {
            return;
        }

        departmentRepository.save(Department.create(departmentCode, departmentName));
    }

    private void createSeedUsers(SeedOrganization seedOrganization, List<SeedUser> seedUsers) {
        if (seedUsers.stream().allMatch(seedUser -> userRepository.existsByLoginId(seedUser.loginId()))) {
            return;
        }

        Organization organization = getOrCreateOrganization(seedOrganization);
        seedUsers.forEach(seedUser -> createUserIfAbsent(organization, seedUser));
    }

    private void createSeedUser(SeedOrganization seedOrganization, SeedUser seedUser) {
        if (userRepository.existsByLoginId(seedUser.loginId())) {
            return;
        }

        Organization organization = getOrCreateOrganization(seedOrganization);
        createUserIfAbsent(organization, seedUser);
    }

    private Organization getOrCreateOrganization(SeedOrganization seedOrganization) {
        return organizationRepository.findByOrganizationAlias(seedOrganization.organizationAlias())
                .orElseGet(() -> organizationRepository.save(
                        Organization.builder()
                                .organizationType(seedOrganization.organizationType())
                                .organizationName(seedOrganization.organizationName())
                                .organizationEnglishName(seedOrganization.organizationEnglishName())
                                .organizationAlias(seedOrganization.organizationAlias())
                                .businessNo(seedOrganization.businessNo())
                                .contactFirstName(seedOrganization.contactFirstName())
                                .contactMiddleName(seedOrganization.contactMiddleName())
                                .contactLastName(seedOrganization.contactLastName())
                                .contactEmail(seedOrganization.contactEmail())
                                .contactPhone(seedOrganization.contactPhone())
                                .status(Status.ACTIVE)
                                .build()
                ));
    }

    private void createUserIfAbsent(Organization organization, SeedUser seedUser) {
        if (userRepository.existsByLoginId(seedUser.loginId())) {
            return;
        }

        userRepository.save(
                User.builder()
                        .organization(organization)
                        .loginId(seedUser.loginId())
                        .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                        .firstName(seedUser.firstName())
                        .middleName(seedUser.middleName())
                        .lastName(seedUser.lastName())
                        .email(seedUser.email())
                        .phone(seedUser.phone())
                        .jobTitle(seedUser.jobTitle())
                        .userRole(seedUser.userRole())
                        .status(Status.ACTIVE)
                        .passwordChangeRequired(false)
                        .build()
        );
    }

    private record SeedOrganization(
            OrganizationType organizationType,
            String organizationName,
            String organizationEnglishName,
            String organizationAlias,
            String businessNo,
            String contactFirstName,
            String contactMiddleName,
            String contactLastName,
            String contactEmail,
            String contactPhone
    ) {
    }

    private record SeedUser(
            String loginId,
            String email,
            String firstName,
            String middleName,
            String lastName,
            String phone,
            String jobTitle,
            UserRole userRole
    ) {
    }
}
