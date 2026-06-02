package com.hwayoung.hwayoungserver.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    private final OpenApiConfig openApiConfig = new OpenApiConfig();

    @Test
    void openAPI_UsesConfiguredServerUrl() {
        OpenAPI openAPI = openApiConfig.openAPI("http://postaserver.haeyul.cloud:8080");

        assertThat(openAPI.getServers()).hasSize(1);
        assertThat(openAPI.getServers().get(0).getUrl()).isEqualTo("http://postaserver.haeyul.cloud:8080");
    }

    @Test
    void openAPI_DoesNotSetServerUrl_WhenConfiguredUrlIsBlank() {
        OpenAPI openAPI = openApiConfig.openAPI("");

        assertThat(openAPI.getServers()).isNull();
    }
}
