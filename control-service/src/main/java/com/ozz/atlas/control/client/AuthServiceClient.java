package com.ozz.atlas.control.client;

import com.ozz.atlas.control.client.dto.AuthUserDetailDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthServiceClient {

    private final RestTemplate restTemplate;

    @Value("${auth-service.url:http://localhost:8081}")
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
}
