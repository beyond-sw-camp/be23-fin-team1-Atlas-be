package com.ozz.atlas.supply.supplier.certificate.service;

import com.ozz.atlas.supply.supplier.certificate.dtos.OrganizationNameLookupResponseDto;
import com.ozz.atlas.supply.supplier.certificate.dtos.ReviewerUserNameLookupResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class CertificateReviewerOrganizationClient {

    private final RestTemplate restTemplate;
    private final String authServiceBaseUrl;

    public CertificateReviewerOrganizationClient(
            RestTemplate restTemplate,
            @Value("${atlas.auth-service.base-url}") String authServiceBaseUrl
    ) {
        this.restTemplate = restTemplate;
        this.authServiceBaseUrl = authServiceBaseUrl;
    }

    public String getOrganizationName(String organizationPublicId) {
        if (organizationPublicId == null || organizationPublicId.isBlank()) {
            return null;
        }

        try {
            String url = authServiceBaseUrl + "/api/auth/organizations/public/" + organizationPublicId + "/name";
            OrganizationNameLookupResponseDto response =
                    restTemplate.getForObject(url, OrganizationNameLookupResponseDto.class);

            if (response == null || response.getOrganizationName() == null || response.getOrganizationName().isBlank()) {
                return organizationPublicId;
            }

            return response.getOrganizationName();
        } catch (RestClientException e) {
            return organizationPublicId;
        }
    }

    public String getUserName(String userPublicId) {
        if (userPublicId == null || userPublicId.isBlank()) {
            return null;
        }

        try {
            String url = authServiceBaseUrl + "/api/auth/users/public/" + userPublicId + "/name";
            ReviewerUserNameLookupResponseDto response =
                    restTemplate.getForObject(url, ReviewerUserNameLookupResponseDto.class);

            if (response == null || response.getUserName() == null || response.getUserName().isBlank()) {
                return userPublicId;
            }

            return response.getUserName();
        } catch (RestClientException e) {
            return userPublicId;
        }
    }

}
