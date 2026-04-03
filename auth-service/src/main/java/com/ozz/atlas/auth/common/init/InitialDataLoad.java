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
    }
}
