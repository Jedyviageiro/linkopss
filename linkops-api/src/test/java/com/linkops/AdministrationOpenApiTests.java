package com.linkops;

import com.jayway.jsonpath.JsonPath;
import com.linkops.user.domain.User;
import com.linkops.user.domain.UserRole;
import com.linkops.user.repository.UserRepository;
import com.linkops.provider.repository.ProviderProfileRepository;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasItems;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdministrationOpenApiTests {

    private static final String ELECTRICIAN_CATEGORY =
            "11000000-0000-0000-0000-000000000001";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ProviderProfileRepository providerProfileRepository;

    @Test
    void shouldAllowOnlyAdminToControlPlatformResources() throws Exception {
        User admin = userRepository.saveAndFlush(new User(
                "Admin",
                "LinkOps",
                "platform.admin@linkops.local",
                null,
                passwordEncoder.encode("Senha-segura-123"),
                UserRole.ADMIN
        ));
        String adminToken = login("platform.admin@linkops.local");

        Registration provider = register(
                "admin.provider@linkops.local", "Paulo", "Prestador", "PROVIDER"
        );
        String profileId = createProviderProfile(provider.token());
        providerProfileRepository.findById(java.util.UUID.fromString(profileId))
                .orElseThrow()
                .updateProfileImageUrl("https://res.cloudinary.com/linkops/provider.jpg");
        mockMvc.perform(post("/providers/me/verification")
                        .header(HttpHeaders.AUTHORIZATION, bearer(provider.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(false))
                .andExpect(jsonPath("$.verificationStatus").value("PENDING"));
        String serviceId = createService(provider.token());
        Registration client = register(
                "admin.client@linkops.local", "Clara", "Cliente", "CLIENT"
        );

        mockMvc.perform(get("/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, bearer(client.token())))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/admin/users")
                .param("sort", "email,asc")
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].email").value(hasItems(
                        "platform.admin@linkops.local",
                        "admin.provider@linkops.local",
                        "admin.client@linkops.local"
                )));

        mockMvc.perform(patch("/admin/providers/{id}/verify", profileId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true))
                .andExpect(jsonPath("$.verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.verificationReviewedBy").value(admin.getId().toString()));

        mockMvc.perform(patch("/admin/providers/{id}/verify", profileId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isConflict());

        mockMvc.perform(patch("/admin/providers/{id}/revoke-verification", profileId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Documento expirado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("REJECTED"))
                .andExpect(jsonPath("$.verified").value(false));

        mockMvc.perform(post("/providers/me/verification")
                        .header(HttpHeaders.AUTHORIZATION, bearer(provider.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("PENDING"));

        mockMvc.perform(patch("/admin/providers/{id}/reject-verification", profileId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Imagem pouco legível\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("REJECTED"))
                .andExpect(jsonPath("$.verificationNote").value("Imagem pouco legível"));

        mockMvc.perform(post("/providers/me/verification")
                        .header(HttpHeaders.AUTHORIZATION, bearer(provider.token())))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/admin/providers/{id}/verify", profileId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true));

        mockMvc.perform(get("/admin/providers")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(profileId));

        mockMvc.perform(patch("/admin/users/{id}/suspend", admin.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/admin/users/{id}/suspend", provider.userId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUSPENDED"));
        mockMvc.perform(get("/providers/{id}", profileId))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(provider.token())))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials("admin.provider@linkops.local")))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(patch("/admin/users/{id}/reactivate", provider.userId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
        mockMvc.perform(get("/providers/{id}", profileId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/admin/services")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(serviceId));
        mockMvc.perform(delete("/admin/services/{id}", serviceId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/services/{id}", serviceId))
                .andExpect(status().isNotFound());

        MvcResult category = mockMvc.perform(post("/admin/categories")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Categoria administrativa\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        String categoryId = JsonPath.read(
                category.getResponse().getContentAsString(), "$.id"
        );
        mockMvc.perform(patch("/admin/categories/{id}", categoryId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Categoria editada\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Categoria editada"));
        mockMvc.perform(delete("/admin/categories/{id}", categoryId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/categories/{id}", categoryId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldExposeSwaggerUiAndBearerOpenApiDescription() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("LinkOps API"))
                .andExpect(jsonPath("$.info.version").value("v1"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme")
                        .value("bearer"))
                .andExpect(jsonPath("$.paths['/admin/users']").exists())
                .andExpect(jsonPath("$.paths['/admin/categories'].post").exists())
                .andExpect(jsonPath("$.paths['/categories'].post").doesNotExist())
                .andExpect(jsonPath("$.paths['/auth/login']").exists())
                .andExpect(jsonPath("$.components.schemas.UserResponse").exists());

        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/swagger-ui/index.html"));
    }

    private Registration register(
            String email,
            String firstName,
            String lastName,
            String role
    ) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName":"%s",
                                  "lastName":"%s",
                                  "email":"%s",
                                  "password":"Senha-segura-123",
                                  "confirmPassword":"Senha-segura-123",
                                  "role":"%s"
                                }
                                """.formatted(firstName, lastName, email, role)))
                .andExpect(status().isCreated())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        return new Registration(
                JsonPath.read(body, "$.accessToken"),
                JsonPath.read(body, "$.user.id")
        );
    }

    private String login(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(email)))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
    }

    private String createProviderProfile(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/providers/profile")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"city\":\"Maputo\",\"bio\":\"Profissional com experiência comprovada.\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }

    private String createService(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/services")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId":"%s",
                                  "title":"Serviço sob moderação",
                                  "price":1000,
                                  "priceType":"FIXED"
                                }
                                """.formatted(ELECTRICIAN_CATEGORY)))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }

    private String credentials(String email) {
        return "{\"email\":\"" + email
                + "\",\"password\":\"Senha-segura-123\"}";
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private record Registration(String token, String userId) {
    }
}
