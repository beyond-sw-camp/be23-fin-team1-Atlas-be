package com.ozz.atlas.supply.logistics.service;

import com.ozz.atlas.supply.logistics.dtos.UserNameLookupResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class LogisticsUserLookupClient {

    private final RestTemplate restTemplate;
    private final String authServiceBaseUrl;

    public LogisticsUserLookupClient(
            RestTemplate restTemplate,
            @Value("${atlas.auth-service.base-url}") String authServiceBaseUrl
    ) {
        this.restTemplate = restTemplate;
        this.authServiceBaseUrl = authServiceBaseUrl;
    }

    public String getUserName(String userPublicId) {
        if (userPublicId == null || userPublicId.isBlank()) {
            return null;
        }

        try {
            String url = authServiceBaseUrl
                    + "/api/auth/users/public/"
                    + userPublicId
                    + "/name";

            UserNameLookupResponseDto response =
                    restTemplate.getForObject(url, UserNameLookupResponseDto.class);

            if (response == null || response.getUserName() == null || response.getUserName().isBlank()) {
                return null;
            }

            return response.getUserName();
        } catch (RestClientException e) {
            return null;
        }
    }
}
