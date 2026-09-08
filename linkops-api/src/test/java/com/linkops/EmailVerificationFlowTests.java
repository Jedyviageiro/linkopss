package com.linkops;

import com.linkops.auth.service.EmailVerificationEmailService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "linkops.security.email-verification.required=true")
@AutoConfigureMockMvc
@Transactional
class EmailVerificationFlowTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmailVerificationEmailService emailVerificationEmailService;

    @Test
    void shouldRequireEmailConfirmationBeforeAuthentication() throws Exception {
        String credentials = """
                {"email":"verify.flow@linkops.local","password":"Senha-segura-123"}
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName":"Ana",
                                  "lastName":"Mabote",
                                  "email":"verify.flow@linkops.local",
                                  "phone":"841234567",
                                  "password":"Senha-segura-123",
                                  "confirmPassword":"Senha-segura-123",
                                  "role":"PROVIDER"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials))
                .andExpect(status().isForbidden());

        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailVerificationEmailService).send(anyString(), tokenCaptor.capture());

        mockMvc.perform(post("/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + tokenCaptor.getValue() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.user.role").value("PROVIDER"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials))
                .andExpect(status().isOk());
    }
}
