package com.linkops;

import com.jayway.jsonpath.JsonPath;
import com.linkops.auth.service.PasswordResetEmailService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PasswordRecoveryFlowTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PasswordResetEmailService passwordResetEmailService;

    @Test
    void shouldConfirmPasswordsAndCompleteOneTimePasswordRecovery() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName":"Lina",
                                  "lastName":"Mondlane",
                                  "email":"password.flow@linkops.local",
                                  "password":"Senha-segura-123",
                                  "confirmPassword":"Outra-senha-123",
                                  "role":"CLIENT"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.passwordConfirmed").exists());

        MvcResult registerResult = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName":"Lina",
                                  "lastName":"Mondlane",
                                  "email":"password.flow@linkops.local",
                                  "password":"Senha-segura-123",
                                  "confirmPassword":"Senha-segura-123",
                                  "role":"CLIENT"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String oldAccessToken = JsonPath.read(
                registerResult.getResponse().getContentAsString(),
                "$.accessToken"
        );

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"password.flow@linkops.local\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").exists());

        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(passwordResetEmailService).send(anyString(), tokenCaptor.capture());
        String resetToken = tokenCaptor.getValue();

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token":"%s",
                                  "password":"Nova-senha-456",
                                  "confirmPassword":"Senha-diferente-456"
                                }
                                """.formatted(resetToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.passwordConfirmed").exists());

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token":"%s",
                                  "password":"Nova-senha-456",
                                  "confirmPassword":"Nova-senha-456"
                                }
                                """.formatted(resetToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("A palavra-passe foi redefinida com sucesso."));

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token":"%s",
                                  "password":"Terceira-senha-789",
                                  "confirmPassword":"Terceira-senha-789"
                                }
                                """.formatted(resetToken)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"password.flow@linkops.local",
                                  "password":"Senha-segura-123"
                                }
                                """))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"password.flow@linkops.local",
                                  "password":"Nova-senha-456"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + oldAccessToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotRevealWhetherEmailExists() throws Exception {
        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"unknown@linkops.local\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value(
                        "Se existir uma conta associada a este e-mail, enviaremos instruções para redefinir a palavra-passe."
                ));
    }
}
