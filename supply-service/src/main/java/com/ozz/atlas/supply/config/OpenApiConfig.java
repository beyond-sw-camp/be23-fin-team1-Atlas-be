package com.ozz.atlas.supply.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Atlas Supply Service API",
                version = "v1",
                description = """
                        Atlas 공급망 운영 API 문서입니다.
                        일부 엔드포인트는 API Gateway가 전달하는 X-Organization-Public-Id, X-User-Public-Id 헤더를 사용합니다.
                        에러 응답은 status, code, message 형식을 사용합니다.
                        """
        ),
        servers = @Server(url = "/", description = "Current server")
)
public class OpenApiConfig {
}
