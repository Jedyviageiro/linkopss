package com.linkops;

import com.linkops.config.CorsConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CorsIntegrationTests {

    private static final String DEVELOPMENT_ORIGIN = "http://localhost:5174";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAllowDevelopmentFrontendPreflightWithBearerHeader() throws Exception {
        mockMvc.perform(options("/auth/login")
                        .header(HttpHeaders.ORIGIN, DEVELOPMENT_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(
                                HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS,
                                "Authorization, Content-Type"
                        ))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        DEVELOPMENT_ORIGIN
                ))
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
                        containsString("POST")
                ))
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                        containsString("Authorization")
                ));
    }

    @Test
    void shouldAddCorsHeadersToActualRequestsAndRejectUnknownOrigins() throws Exception {
        mockMvc.perform(get("/categories")
                        .header(HttpHeaders.ORIGIN, DEVELOPMENT_ORIGIN))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        DEVELOPMENT_ORIGIN
                ));

        mockMvc.perform(options("/auth/login")
                        .header(HttpHeaders.ORIGIN, "https://origem-nao-autorizada.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN
                ));
    }

    @Test
    void shouldRejectWildcardOrMalformedCorsConfiguration() {
        CorsConfig corsConfig = new CorsConfig();

        assertThatThrownBy(() -> corsConfig.corsConfigurationSource(
                java.util.List.of("*")
        )).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> corsConfig.corsConfigurationSource(
                java.util.List.of("https://app.linkops.example/path")
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
