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

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SearchBookingFlowTests {

    private static final String ELECTRICIAN_CATEGORY =
            "11000000-0000-0000-0000-000000000001";
    private static final String PLUMBER_CATEGORY =
            "11000000-0000-0000-0000-000000000002";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCombineServiceAndProviderSearchFilters() throws Exception {
        ProviderData provider = createProvider(
                "search.provider@linkops.local", "Ana", "Maputo", "Maputo"
        );
        createService(provider.token(), ELECTRICIAN_CATEGORY,
                "Instalação elétrica", "1500.00", "FIXED");
        createService(provider.token(), PLUMBER_CATEGORY,
                "Canalização residencial", "800.00", "FIXED");

        mockMvc.perform(get("/services")
                        .param("q", "Instalação")
                        .param("category", "eletricidade")
                        .param("city", "maputo")
                        .param("minPrice", "1000")
                        .param("maxPrice", "2000")
                        .param("sort", "price,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Instalação elétrica"))
                .andExpect(jsonPath("$.content[0].city").value("Maputo"))
                .andExpect(jsonPath("$.content[0].latitude").value(-25.9653))
                .andExpect(jsonPath("$.content[0].longitude").value(32.5892));

        mockMvc.perform(get("/services")
                        .param("category", "canalizacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].categoryName").value("Canalizador"));

        mockMvc.perform(get("/providers")
                        .param("category", "eletricidade")
                        .param("city", "Maputo")
                        .param("sort", "rating,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(provider.profileId()));

        mockMvc.perform(get("/services")
                        .param("minPrice", "2000")
                        .param("maxPrice", "1000"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/services").param("sort", "passwordHash,asc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldEnforceBookingOwnershipAndStateTransitions() throws Exception {
        ProviderData owner = createProvider(
                "booking.provider@linkops.local", "Paulo", "Prestador", "Maputo"
        );
        ProviderData outsiderProvider = createProvider(
                "booking.outsider@linkops.local", "Outro", "Prestador", "Matola"
        );
        String serviceId = createService(
                owner.token(), ELECTRICIAN_CATEGORY,
                "Reparação elétrica", "1200.00", "FIXED"
        );
        String clientToken = registerUser(
                "booking.client@linkops.local", "Clara", "Cliente", "CLIENT"
        );
        String outsiderClientToken = registerUser(
                "booking.other.client@linkops.local", "Outro", "Cliente", "CLIENT"
        );

        String bookingId = createBooking(clientToken, serviceId, 2);

        mockMvc.perform(get("/bookings")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + owner.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));

        mockMvc.perform(get("/bookings/{id}", bookingId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + outsiderClientToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/bookings/{id}/accept", bookingId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + outsiderProvider.token()))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/bookings/{id}/accept", bookingId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + owner.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        mockMvc.perform(patch("/bookings/{id}/start", bookingId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + owner.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(patch("/bookings/{id}/cancel", bookingId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientToken))
                .andExpect(status().isConflict());

        mockMvc.perform(patch("/bookings/{id}/complete", bookingId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + owner.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        String cancellableId = createBooking(clientToken, serviceId, 3);
        mockMvc.perform(patch("/bookings/{id}/cancel", cancellableId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        String rejectableId = createBooking(clientToken, serviceId, 4);
        mockMvc.perform(patch("/bookings/{id}/reject", rejectableId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + owner.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    private ProviderData createProvider(
            String email,
            String firstName,
            String lastName,
            String city
    ) throws Exception {
        String token = registerUser(email, firstName, lastName, "PROVIDER");
        MvcResult result = mockMvc.perform(post("/providers/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "city": "%s",
                                  "latitude": -25.965300,
                                  "longitude": 32.589200
                                }
                                """.formatted(city)))
                .andExpect(status().isCreated())
                .andReturn();
        return new ProviderData(
                token,
                JsonPath.read(result.getResponse().getContentAsString(), "$.id")
        );
    }

    private String registerUser(
            String email,
            String firstName,
            String lastName,
            String role
    ) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "%s",
                                  "lastName": "%s",
                                  "email": "%s",
                                  "password": "Senha-segura-123",
                                  "confirmPassword": "Senha-segura-123",
                                  "role": "%s"
                                }
                                """.formatted(firstName, lastName, email, role)))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
    }

    private String createService(
            String providerToken,
            String categoryId,
            String title,
            String price,
            String priceType
    ) throws Exception {
        MvcResult result = mockMvc.perform(post("/services")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + providerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": "%s",
                                  "title": "%s",
                                  "price": %s,
                                  "priceType": "%s"
                                }
                                """.formatted(categoryId, title, price, priceType)))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }

    private String createBooking(String clientToken, String serviceId, int days) throws Exception {
        String scheduledAt = Instant.now().plus(days, ChronoUnit.DAYS).toString();
        MvcResult result = mockMvc.perform(post("/bookings")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "serviceOfferingId": "%s",
                                  "scheduledAt": "%s",
                                  "address": "Av. Julius Nyerere, Maputo",
                                  "notes": "Contactar antes da visita",
                                  "paymentMethod": "MPESA"
                                }
                                """.formatted(serviceId, scheduledAt)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }

    private record ProviderData(String token, String profileId) {
    }
}
