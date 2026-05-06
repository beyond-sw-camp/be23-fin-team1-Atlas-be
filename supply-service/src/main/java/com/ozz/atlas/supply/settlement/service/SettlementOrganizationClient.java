package com.ozz.atlas.supply.settlement.service;

import com.ozz.atlas.supply.settlement.dtos.OrganizationNameLookupResponseDto;
import com.ozz.atlas.supply.settlement.dtos.UserNameLookupResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

// 정산 엑셀에서 조직 Public ID 대신 조직명을 보여주기 위해 auth-service를 호출
@Component
public class SettlementOrganizationClient {

    private final RestTemplate restTemplate;
    private final String authServiceBaseUrl;

    public SettlementOrganizationClient(
            RestTemplate restTemplate,
            @Value("${atlas.auth-service.base-url}") String authServiceBaseUrl
    ) {
        this.restTemplate = restTemplate;
        this.authServiceBaseUrl = authServiceBaseUrl;
    }

    public OrganizationNameLookupResponseDto getOrganization(String organizationPublicId) {
        if (organizationPublicId == null || organizationPublicId.isBlank()) {
            return new OrganizationNameLookupResponseDto();
        }

        try {
            String url = authServiceBaseUrl
                    + "/api/auth/organizations/public/"
                    + organizationPublicId
                    + "/name";

            OrganizationNameLookupResponseDto response =
                    restTemplate.getForObject(url, OrganizationNameLookupResponseDto.class);

            if (response == null) {
                return fallbackOrganization(organizationPublicId);
            }

            return response;
        } catch (RestClientException e) {
            return fallbackOrganization(organizationPublicId);
        }
    }

    private OrganizationNameLookupResponseDto fallbackOrganization(String organizationPublicId) {
        OrganizationNameLookupResponseDto response = new OrganizationNameLookupResponseDto();
        response.setOrganizationPublicId(organizationPublicId);
        response.setOrganizationName(organizationPublicId);
        return response;
    }
    public UserNameLookupResponseDto getUserName(String userPublicId) {
        if (userPublicId == null || userPublicId.isBlank()) {
            return fallbackUser(userPublicId);
        }

        try {
            String url = authServiceBaseUrl
                    + "/api/auth/users/public/"
                    + userPublicId
                    + "/name";

            UserNameLookupResponseDto response =
                    restTemplate.getForObject(url, UserNameLookupResponseDto.class);

            if (response == null) {
                return fallbackUser(userPublicId);
            }

            return response;
        } catch (RestClientException e) {
            return fallbackUser(userPublicId);
        }
    }

    private UserNameLookupResponseDto fallbackUser(String userPublicId) {
        UserNameLookupResponseDto response = new UserNameLookupResponseDto();
        response.setUserPublicId(userPublicId);
        response.setUserName(userPublicId == null || userPublicId.isBlank() ? "-" : userPublicId);
        return response;
    }
}
