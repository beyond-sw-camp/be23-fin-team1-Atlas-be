package com.ozz.atlas.supply.logistics.service;

import com.ozz.atlas.supply.logistics.dtos.OrganizationAliasLookupResponseDto;
import com.ozz.atlas.supply.logistics.exception.LogisticsNodeErrorCode;
import com.ozz.atlas.supply.logistics.exception.LogisticsNodeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class OrganizationAliasClient {

    private final RestTemplate restTemplate;
    private final String authServiceBaseUrl;

    public OrganizationAliasClient(
            RestTemplate restTemplate,
            @Value("${atlas.auth-service.base-url}") String authServiceBaseUrl
    ) {
        this.restTemplate = restTemplate;
        this.authServiceBaseUrl = authServiceBaseUrl;
    }

    // 물류거점 코드 생성 전 organizationPublicId로 조직 alias를 조회한다.
    public String getOrganizationAlias(String organizationPublicId) {
        try {
            String url = authServiceBaseUrl + "/api/auth/organizations/public/" + organizationPublicId + "/alias";

            OrganizationAliasLookupResponseDto response =
                    restTemplate.getForObject(url, OrganizationAliasLookupResponseDto.class);

            if (response == null || response.getOrganizationAlias() == null || response.getOrganizationAlias().isBlank()) {
                throw new LogisticsNodeException(LogisticsNodeErrorCode.INVALID_INPUT_VALUE);
            }

            return response.getOrganizationAlias().trim().toUpperCase();
        } catch (RestClientException e) {
            throw new LogisticsNodeException(LogisticsNodeErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
