package com.ozz.atlas.control.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Atlas Control Service API",
                version = "v1",
                description = """
                        Atlas 관제, 채팅, 알림 API 문서입니다.
                        사용자 식별이 필요한 엔드포인트는 X-User-Public-Id 헤더를 사용합니다.
                        에러 응답은 status, code, message 형식을 사용합니다.
                        """
        ),
        servers = @Server(url = "/", description = "Current server")
)
public class OpenApiConfig {
}
