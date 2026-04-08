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

    @Value("${supply-service.url:http://localhost:8080}")
    private String supplyServiceUrl;

    public boolean validateReturnRequest(String publicId) {
        try {
            String url = supplyServiceUrl + "/api/v1/supply/returns/" + publicId;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException e) {
            log.error("Failed to validate ReturnRequest {}: {}", publicId, e.getMessage());
            return false;
        }
    }

    public boolean validatePurchaseOrder(String publicId) {
        try {
            String url = supplyServiceUrl + "/api/v1/supply/purchase-orders/" + publicId;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException e) {
            log.error("Failed to validate PurchaseOrder {}: {}", publicId, e.getMessage());
            return false;
        }
    }
    
    public boolean validateItem(String publicId) {
        try {
            String url = supplyServiceUrl + "/api/v1/supply/items/" + publicId;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException e) {
            log.error("Failed to validate Item {}: {}", publicId, e.getMessage());
            return false;
        }
    }
}