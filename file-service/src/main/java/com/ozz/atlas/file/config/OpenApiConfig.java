package com.ozz.atlas.file.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Atlas File Service API",
                version = "v1",
                description = """
                        Atlas 첨부 파일 관리 API 문서입니다.
                        업로드 및 수정 엔드포인트는 multipart/form-data와 X-User-Public-Id 헤더를 함께 사용합니다.
                        에러 응답은 status, code, message 형식을 사용합니다.
                        """
        ),
        servers = @Server(url = "/", description = "Current server")
)
public class OpenApiConfig {
}
