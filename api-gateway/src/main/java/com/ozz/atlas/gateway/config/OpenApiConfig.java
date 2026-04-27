package com.ozz.atlas.gateway.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Atlas API Gateway API",
                version = "v1",
                description = """
                        Atlas API Gateway 자체 제공 API 문서입니다.
                        서비스별 API 문서는 각 서비스 Swagger 문서를 기준으로 확인합니다.
                        """
        ),
        servers = @Server(url = "/", description = "Current server")
)
public class OpenApiConfig {
}
