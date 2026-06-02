package com.hwayoung.hwayoungserver.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.List;

@Configuration
public class OpenApiConfig {
    private static final String JWT_SECURITY_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI openAPI(@Value("${app.openapi.server-url:}") String serverUrl) {
        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title("Hwayoung Server API")
                        .description("Hwayoung Spring Boot backend API documentation")
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes(JWT_SECURITY_SCHEME, new SecurityScheme()
                                .name(JWT_SECURITY_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(JWT_SECURITY_SCHEME));

        if (StringUtils.hasText(serverUrl)) {
            openAPI.servers(List.of(new Server().url(serverUrl)));
        }

        return openAPI;
    }
}
