package com.ozz.atlas.auth.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Atlas Auth Service API",
                version = "v1",
                description = """
                        Atlas 인증, 사용자, 조직 관리 API 문서입니다.
                        에러 응답은 status, code, message 형식을 사용합니다.
                        """
        ),
        servers = @Server(url = "/", description = "Current server")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Authorization 헤더에 Bearer Access Token을 전달합니다."
)
public class OpenApiConfig {
}
