package com.linkops;

import com.jayway.jsonpath.JsonPath;
import com.linkops.media.service.CloudMediaStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MediaFlowTests {

    private static final String ELECTRICIAN_CATEGORY =
            "11000000-0000-0000-0000-000000000001";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CloudMediaStorage cloudStorage;

    @BeforeEach
    void configureCloudStorage() {
        when(cloudStorage.upload(any(byte[].class), anyString(), anyString(), anyString()))
                .thenAnswer(invocation -> new CloudMediaStorage.StoredMedia(
                        "https://res.cloudinary.com/linkops/image/upload/test-image.png",
                        invocation.getArgument(1),
                        ((byte[]) invocation.getArgument(0)).length
                ));
    }

    @Test
    void shouldNormalizeLocationAndUploadProfileAndServiceImages() throws Exception {
        String token = registerProvider();
        MvcResult profileResult = mockMvc.perform(post("/providers/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "city":"  Maputo   Cidade ",
                                  "latitude":-25.965300,
                                  "longitude":32.589200
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.city").value("Maputo Cidade"))
                .andExpect(jsonPath("$.location.coordinatesAvailable").value(true))
                .andReturn();
        String profileId = JsonPath.read(
                profileResult.getResponse().getContentAsString(), "$.id"
        );

        MockMultipartFile image = pngImage();
        mockMvc.perform(multipart("/media/providers/profile-image")
                        .file(image)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resourceId").value(profileId))
                .andExpect(jsonPath("$.resourceType").value("PROVIDER_PROFILE"));

        String serviceId = createService(token);
        mockMvc.perform(multipart("/media/services/{id}/images", serviceId)
                        .file(pngImage())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resourceType").value("SERVICE_IMAGE"))
                .andExpect(jsonPath("$.url").value(
                        "https://res.cloudinary.com/linkops/image/upload/test-image.png"
                ));

        mockMvc.perform(get("/media/services/{id}/images", serviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        MockMultipartFile invalid = new MockMultipartFile(
                "file", "fake.png", MediaType.IMAGE_PNG_VALUE,
                "isto não é uma imagem".getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
        mockMvc.perform(multipart("/media/services/{id}/images", serviceId)
                        .file(invalid)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    private String registerProvider() throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName":"Marta",
                                  "lastName":"Media",
                                  "email":"media.provider@linkops.local",
                                  "password":"Senha-segura-123",
                                  "role":"PROVIDER"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
    }

    private String createService(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/services")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId":"%s",
                                  "title":"Instalação elétrica com imagem",
                                  "price":1200,
                                  "priceType":"FIXED"
                                }
                                """.formatted(ELECTRICIAN_CATEGORY)))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }

    private MockMultipartFile pngImage() {
        byte[] content = new byte[]{
                (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x00
        };
        return new MockMultipartFile(
                "file", "imagem.png", MediaType.IMAGE_PNG_VALUE, content
        );
    }
}
