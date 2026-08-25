package com.linkops;

import com.jayway.jsonpath.JsonPath;
import com.linkops.user.domain.User;
import com.linkops.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityHardeningTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldExposeOnlyTheRequiredPublicAuthenticationMethods() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldHashPasswordAndNeverReturnItsHash() throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName":"Sara",
                                  "lastName":"Segura",
                                  "email":"sara.security@linkops.local",
                                  "password":"Senha-segura-123",
                                  "role":"CLIENT"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.user.password").doesNotExist())
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist())
                .andReturn();

        String userId = JsonPath.read(result.getResponse().getContentAsString(), "$.user.id");
        User user = userRepository.findById(java.util.UUID.fromString(userId)).orElseThrow();
        assertThat(user.getPasswordHash()).startsWith("$2");
        assertThat(passwordEncoder.matches("Senha-segura-123", user.getPasswordHash())).isTrue();
    }

    @Test
    void shouldRejectUnknownOrOversizedInputFields() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"nobody@linkops.local",
                                  "password":"Senha-segura-123",
                                  "administrator":true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Os dados enviados são inválidos ou estão incompletos."
                ));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nobody@linkops.local\",\"password\":\""
                                + "a".repeat(73) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.password").exists());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nobody@linkops.local\",\"password\":\""
                                + "🔒".repeat(20) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.password").exists());
    }

    @Test
    void shouldReturnSafeErrorsForInvalidIdsAndSearchInput() throws Exception {
        mockMvc.perform(get("/services/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Os dados enviados são inválidos ou estão incompletos."
                ))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.exception").doesNotExist());

        mockMvc.perform(get("/services").param("q", "a".repeat(201)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldOnlyAcceptProviderImagesThroughValidatedUploads() throws Exception {
        MvcResult registration = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName":"Paulo",
                                  "lastName":"Protegido",
                                  "email":"paulo.protected@linkops.local",
                                  "password":"Senha-segura-123",
                                  "role":"PROVIDER"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String token = JsonPath.read(
                registration.getResponse().getContentAsString(), "$.accessToken"
        );

        mockMvc.perform(post("/providers/profile")
                        .header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "city":"Maputo",
                                  "profileImageUrl":"https://attacker.example/image.svg"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.profileImageUrl").exists());
    }
}
