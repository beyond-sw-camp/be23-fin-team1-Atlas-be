package com.ozz.atlas.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    // eureka 에 내부서비스 호출용 annotation
    // @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
