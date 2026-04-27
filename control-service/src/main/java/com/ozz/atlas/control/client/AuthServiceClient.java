package com.ozz.atlas.control.client;

import com.ozz.atlas.control.client.dto.AuthUserDetailDto;
import com.ozz.atlas.control.client.dto.AuthUserRecipientDto;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthServiceClient {

    private final RestTemplate restTemplate;

    @Value("${auth-service.url}")
    private String authServiceUrl;

    // userPublicId 기준으로 auth-service 에 사용자 상세정보를 요청
    // 채팅 참가자 검색 문서를 만들 때 loginId, 이름, email 을 채우는 용도로 사용
    public AuthUserDetailDto getUserDetailByPublicId(String userPublicId) {
        try {
            String url = authServiceUrl + "/api/auth/users/public/" + userPublicId;
            return restTemplate.getForObject(url, AuthUserDetailDto.class);
        } catch (RestClientException e) {
            // auth-service 호출 실패 시 예외를 바로 터뜨리지 않고 null 을 반환
            // 채팅 색인 과정에서 전체 흐름이 중단되지 않게 하기 위한 방어 로직
            log.error("Failed to fetch user {} from auth-service: {}", userPublicId, e.getMessage());
            return null;
        }
    }

    public List<AuthUserRecipientDto> getNotificationRecipients(
            String organizationPublicId,
            String departmentCode
    ) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(authServiceUrl + "/api/auth/internal/users/recipients")
                    .queryParam("organizationPublicId", organizationPublicId)
                    .queryParamIfPresent("departmentCode", java.util.Optional.ofNullable(departmentCode))
                    .toUriString();
            AuthUserRecipientDto[] response = restTemplate.getForObject(url, AuthUserRecipientDto[].class);
            return response == null ? List.of() : Arrays.asList(response);
        } catch (RestClientException e) {
            log.error("Failed to fetch notification recipients. organizationPublicId={}, departmentCode={}, error={}",
                    organizationPublicId, departmentCode, e.getMessage());
            return List.of();
        }
    }
}
