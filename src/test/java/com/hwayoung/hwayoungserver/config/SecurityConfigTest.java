package com.hwayoung.hwayoungserver.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig(null, null);
    private final List<String> allowedOriginPatterns = List.of(
            "http://localhost:*",
            "https://localhost:*",
            "http://postaserver.haeyul.cloud",
            "http://postaserver.haeyul.cloud:*"
    );

    @Test
    void corsConfiguration_AllowsLocalhostOrigin() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource(allowedOriginPatterns);
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/v1/portfolios");
        request.addHeader("Origin", "http://localhost:5173");

        CorsConfiguration configuration = source.getCorsConfiguration(request);

        assertThat(configuration).isNotNull();
        assertThat(configuration.checkOrigin("http://localhost:5173")).isEqualTo("http://localhost:5173");
    }

    @Test
    void corsConfiguration_AllowsDeploymentOrigin() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource(allowedOriginPatterns);
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/v1/auth/signup");
        request.addHeader("Origin", "http://postaserver.haeyul.cloud");

        CorsConfiguration configuration = source.getCorsConfiguration(request);

        assertThat(configuration).isNotNull();
        assertThat(configuration.checkOrigin("http://postaserver.haeyul.cloud")).isEqualTo("http://postaserver.haeyul.cloud");
    }
}
