package com.ozz.atlas.supply.logistics.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.ozz.atlas.supply.logistics.dtos.GeocodingPointDto;
import com.ozz.atlas.supply.logistics.exception.LogisticsNodeErrorCode;
import com.ozz.atlas.supply.logistics.exception.LogisticsNodeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.nio.charset.StandardCharsets;
import java.net.URI;

import java.math.BigDecimal;

@Component
public class AddressGeocodingClient {

    private final RestTemplate restTemplate;
    private final String kakaoBaseUrl;
    private final String kakaoRestApiKey;

    public AddressGeocodingClient(
            RestTemplate restTemplate,
            @Value("${atlas.geocoding.kakao.base-url}") String kakaoBaseUrl,
            @Value("${atlas.geocoding.kakao.rest-api-key}") String kakaoRestApiKey
    ) {
        this.restTemplate = restTemplate;
        this.kakaoBaseUrl = kakaoBaseUrl;
        this.kakaoRestApiKey = kakaoRestApiKey;
    }

    // 주소를 저장 직전에 좌표로 변환하고, 실패하면 저장을 막는다.
    public GeocodingPointDto geocode(String address) {
        if (address == null || address.isBlank()) {
            throw new LogisticsNodeException(LogisticsNodeErrorCode.INVALID_INPUT_VALUE);
        }

        if (kakaoRestApiKey == null || kakaoRestApiKey.isBlank()) {
            throw new LogisticsNodeException(LogisticsNodeErrorCode.ADDRESS_GEOCODING_FAILED);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoRestApiKey);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            URI uri = UriComponentsBuilder
                    .fromHttpUrl(kakaoBaseUrl)
                    .path("/v2/local/search/address.json")
                    .queryParam("query", address)
                    .build()
                    .encode(StandardCharsets.UTF_8)
                    .toUri();

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    entity,
                    JsonNode.class
            );


            JsonNode body = response.getBody();
            JsonNode documents = body == null ? null : body.path("documents");

            if (documents == null || !documents.isArray() || documents.isEmpty()) {
                throw new LogisticsNodeException(LogisticsNodeErrorCode.ADDRESS_GEOCODING_FAILED);
            }

            JsonNode first = documents.get(0);
            String longitude = first.path("x").asText();
            String latitude = first.path("y").asText();

            if (longitude == null || longitude.isBlank() || latitude == null || latitude.isBlank()) {
                throw new LogisticsNodeException(LogisticsNodeErrorCode.ADDRESS_GEOCODING_FAILED);
            }

            return new GeocodingPointDto(
                    new BigDecimal(latitude),
                    new BigDecimal(longitude)
            );
        } catch (RestClientException | NumberFormatException e) {
            throw new LogisticsNodeException(LogisticsNodeErrorCode.ADDRESS_GEOCODING_FAILED);
        }
    }
}
