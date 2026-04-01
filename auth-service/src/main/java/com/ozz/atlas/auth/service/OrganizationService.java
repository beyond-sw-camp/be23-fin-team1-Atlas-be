package com.ozz.atlas.auth.service;

import com.ozz.atlas.auth.domain.Organization;
import com.ozz.atlas.auth.dtos.OrganizationCreateDto;
import com.ozz.atlas.auth.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrganizationService {
    private final OrganizationRepository organizationRepository;
    @Autowired
    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }
//    조직 생성
    public String createOrganization(OrganizationCreateDto dto) {
        Organization organization = organizationRepository.save(dto.toEntity());
        return organization.getPublicId();
    }

}
