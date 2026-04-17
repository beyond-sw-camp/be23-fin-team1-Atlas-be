package com.ozz.atlas.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    // 게이트웨이가 다른 서비스 검색 API를 호출할 때 사용할 WebClient
    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }
}
