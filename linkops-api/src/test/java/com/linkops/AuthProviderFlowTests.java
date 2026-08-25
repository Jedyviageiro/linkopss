package com.linkops;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthProviderFlowTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRegisterAuthenticateAndCreateProviderProfile() throws Exception {
        String registerBody = """
                {
                  "firstName": "Ana",
                  "lastName": "Matola",
                  "email": "provider.flow.test@linkops.local",
                  "phone": "+258840000001",
                  "password": "Senha-segura-123",
                  "confirmPassword": "Senha-segura-123",
                  "role": "PROVIDER"
                }
                """;

        MvcResult registerResult = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.user.role").value("PROVIDER"))
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist())
                .andReturn();

        String registerJson = registerResult.getResponse().getContentAsString();
        String accessToken = JsonPath.read(registerJson, "$.accessToken");
        String refreshToken = JsonPath.read(registerJson, "$.refreshToken");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("provider.flow.test@linkops.local"));

        MvcResult profileResult = mockMvc.perform(post("/providers/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bio": "Tecnica profissional",
                                  "city": "Maputo",
                                  "latitude": -25.965300,
                                  "longitude": 32.589200
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.verified").value(false))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn();

        String profileId = JsonPath.read(
                profileResult.getResponse().getContentAsString(),
                "$.id"
        );

        mockMvc.perform(post("/providers/me/verification")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Adicione uma biografia e uma imagem de perfil antes de solicitar a verificação."
                ));

        mockMvc.perform(get("/providers/{id}", profileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Maputo"));

        mockMvc.perform(get("/providers/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(profileId));

        mockMvc.perform(patch("/providers/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bio":"Especialista em instalações e manutenção",
                                  "city":"Matola",
                                  "latitude":-25.9622,
                                  "longitude":32.4589
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Matola"));

        mockMvc.perform(patch("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName":"Ana Maria",
                                  "phone":"+258850000001"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Ana Maria"))
                .andExpect(jsonPath("$.phone").value("+258850000001"));

        mockMvc.perform(get("/providers?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0]").exists());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "provider.flow.test@linkops.local",
                                  "password": "Senha-segura-123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void shouldRejectClientProviderProfileCreation() throws Exception {
        MvcResult registerResult = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Carlos",
                                  "lastName": "Tembe",
                                  "email": "client.flow.test@linkops.local",
                                  "password": "Senha-segura-123",
                                  "confirmPassword": "Senha-segura-123",
                                  "role": "CLIENT"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String accessToken = JsonPath.read(
                registerResult.getResponse().getContentAsString(),
                "$.accessToken"
        );

        mockMvc.perform(post("/providers/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"city\":\"Maputo\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Acesso negado"));
    }
}
