package com.ozz.atlas.control.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class SupplyServiceClient {

    private final RestTemplate restTemplate;

    @Value("${supply-service.url}")
    private String supplyServiceUrl;

    public boolean validateReturnRequest(String publicId) {
        try {
            String url = supplyServiceUrl + "/api/supply/returns/" + publicId + "/exists";
            ResponseEntity<Boolean> response = restTemplate.getForEntity(url, Boolean.class);
            return response.getStatusCode().is2xxSuccessful() && Boolean.TRUE.equals(response.getBody());
        } catch (RestClientException e) {
            log.error("Failed to validate ReturnRequest {}: {}", publicId, e.getMessage());
            return false;
        }
    }

    public boolean validatePurchaseOrder(String publicId) {
        try {
            String url = supplyServiceUrl + "/api/supply/purchase-order/" + publicId;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException e) {
            log.error("Failed to validate PurchaseOrder {}: {}", publicId, e.getMessage());
            return false;
        }
    }
    
    public boolean validateItem(String publicId) {
        try {
            String url = supplyServiceUrl + "/api/supply/items/" + publicId;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException e) {
            log.error("Failed to validate Item {}: {}", publicId, e.getMessage());
            return false;
        }
    }
}