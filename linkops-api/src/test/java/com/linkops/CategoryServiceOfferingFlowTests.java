package com.linkops;

import com.jayway.jsonpath.JsonPath;
import com.linkops.security.AuthenticatedUser;
import com.linkops.security.JwtService;
import com.linkops.user.domain.User;
import com.linkops.user.domain.UserRole;
import com.linkops.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoryServiceOfferingFlowTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Test
    void shouldExposeSeededCategoriesAndRestrictManagementToAdmin() throws Exception {
        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[?(@.name == 'Reparações para Casa')].children.length()")
                        .value(5));

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Categoria protegida\"}"))
                .andExpect(status().isUnauthorized());

        User admin = userRepository.saveAndFlush(new User(
                "Admin", "LinkOps", "category.admin@linkops.local", null,
                passwordEncoder.encode("Senha-segura-123"), UserRole.ADMIN
        ));
        String adminToken = jwtService.generateAccessToken(AuthenticatedUser.from(admin));

        MvcResult createResult = mockMvc.perform(post("/admin/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Formação Profissional\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("formacao-profissional"))
                .andReturn();

        String categoryId = JsonPath.read(
                createResult.getResponse().getContentAsString(), "$.id"
        );
        mockMvc.perform(patch("/admin/categories/{id}", categoryId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Formação e Cursos\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("formacao-e-cursos"));
    }

    @Test
    void shouldPublishUpdateListAndDeactivateOnlyOwnedService() throws Exception {
        ProviderCredentials owner = registerProviderWithProfile(
                "service.owner@linkops.local", "Prestador", "Um"
        );
        ProviderCredentials other = registerProviderWithProfile(
                "service.other@linkops.local", "Prestador", "Dois"
        );

        String categoryId = "11000000-0000-0000-0000-000000000001";
        MvcResult createResult = mockMvc.perform(post("/services")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + owner.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": "%s",
                                  "title": "Instalação elétrica residencial",
                                  "description": "Instalação e reparação",
                                  "price": 1500.00,
                                  "priceType": "FIXED"
                                }
                                """.formatted(categoryId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryName").value("Electricista"))
                .andExpect(jsonPath("$.active").value(true))
                .andReturn();

        String serviceId = JsonPath.read(
                createResult.getResponse().getContentAsString(), "$.id"
        );

        mockMvc.perform(get("/services/{id}", serviceId))
                .andExpect(status().isOk());
        mockMvc.perform(get("/services?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0]").exists());
        mockMvc.perform(get("/providers/{providerId}/services", owner.profileId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(serviceId));

        mockMvc.perform(patch("/services/{id}", serviceId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + other.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Alteração indevida\"}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/services/{id}", serviceId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + owner.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"priceType\":\"NEGOTIABLE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priceType").value("NEGOTIABLE"))
                .andExpect(jsonPath("$.price").doesNotExist());

        mockMvc.perform(delete("/services/{id}", serviceId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + owner.token()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/services/{id}", serviceId))
                .andExpect(status().isNotFound());
    }

    private ProviderCredentials registerProviderWithProfile(
            String email,
            String firstName,
            String lastName
    ) throws Exception {
        MvcResult registerResult = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "%s",
                                  "lastName": "%s",
                                  "email": "%s",
                                  "password": "Senha-segura-123",
                                  "confirmPassword": "Senha-segura-123",
                                  "role": "PROVIDER"
                                }
                                """.formatted(firstName, lastName, email)))
                .andExpect(status().isCreated())
                .andReturn();
        String token = JsonPath.read(
                registerResult.getResponse().getContentAsString(), "$.accessToken"
        );

        MvcResult profileResult = mockMvc.perform(post("/providers/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"city\":\"Maputo\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        String profileId = JsonPath.read(
                profileResult.getResponse().getContentAsString(), "$.id"
        );
        return new ProviderCredentials(token, profileId);
    }

    private record ProviderCredentials(String token, String profileId) {
    }
}
