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
class NotificationFlowTests {

    private static final String CATEGORY_ID =
            "11000000-0000-0000-0000-000000000001";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateListAndProtectBookingAndReviewNotifications() throws Exception {
        String providerToken = register(
                "notification.provider@linkops.local", "Paulo", "Prestador", "PROVIDER"
        );
        createProviderProfile(providerToken);
        String serviceId = createService(providerToken);
        String clientToken = register(
                "notification.client@linkops.local", "Clara", "Cliente", "CLIENT"
        );

        String bookingId = createBooking(clientToken, serviceId, 2);
        MvcResult createdNotification = mockMvc.perform(get("/notifications")
                        .header(HttpHeaders.AUTHORIZATION, bearer(providerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].type").value("BOOKING_CREATED"))
                .andExpect(jsonPath("$.content[0].title").value("Novo pedido de serviço"))
                .andExpect(jsonPath("$.content[0].message")
                        .value("Clara Cliente solicitou o serviço \"Eletricidade residencial\"."))
                .andExpect(jsonPath("$.content[0].read").value(false))
                .andReturn();
        String notificationId = JsonPath.read(
                createdNotification.getResponse().getContentAsString(),
                "$.content[0].id"
        );

        mockMvc.perform(patch("/notifications/{id}/read", notificationId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(clientToken)))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/notifications/{id}/read", notificationId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(providerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true))
                .andExpect(jsonPath("$.readAt").isNotEmpty());

        mockMvc.perform(patch("/bookings/{id}/accept", bookingId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(providerToken)))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/bookings/{id}/start", bookingId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(providerToken)))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/bookings/{id}/complete", bookingId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(providerToken)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/notifications")
                        .header(HttpHeaders.AUTHORIZATION, bearer(clientToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].type").value("BOOKING_COMPLETED"))
                .andExpect(jsonPath("$.content[1].type").value("BOOKING_ACCEPTED"));

        mockMvc.perform(post("/bookings/{id}/review", bookingId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(clientToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":5,\"comment\":\"Excelente serviço.\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/notifications")
                        .header(HttpHeaders.AUTHORIZATION, bearer(providerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].type").value("REVIEW_RECEIVED"))
                .andExpect(jsonPath("$.content[0].message")
                        .value("Clara Cliente avaliou o seu serviço com 5 de 5 estrelas."));

        String rejectedBooking = createBooking(clientToken, serviceId, 3);
        mockMvc.perform(patch("/bookings/{id}/reject", rejectedBooking)
                        .header(HttpHeaders.AUTHORIZATION, bearer(providerToken)))
                .andExpect(status().isOk());

        String cancelledBooking = createBooking(clientToken, serviceId, 4);
        mockMvc.perform(patch("/bookings/{id}/cancel", cancelledBooking)
                        .header(HttpHeaders.AUTHORIZATION, bearer(clientToken)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/notifications")
                        .param("sort", "createdAt,desc")
                        .header(HttpHeaders.AUTHORIZATION, bearer(clientToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].type").value("BOOKING_REJECTED"));

        mockMvc.perform(get("/notifications")
                        .header(HttpHeaders.AUTHORIZATION, bearer(providerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].type").value("BOOKING_CANCELLED"));
    }

    @Test
    void shouldRequireAuthenticationAndRejectUnsafeSorting() throws Exception {
        mockMvc.perform(get("/notifications"))
                .andExpect(status().isUnauthorized());

        String token = register(
                "notification.sort@linkops.local", "Ana", "Cliente", "CLIENT"
        );
        mockMvc.perform(get("/notifications")
                        .param("sort", "recipient.passwordHash,asc")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isBadRequest());
    }

    private String register(String email, String firstName, String lastName, String role)
            throws Exception {
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
        return JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
    }

    private void createProviderProfile(String token) throws Exception {
        mockMvc.perform(post("/providers/profile")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"city\":\"Maputo\"}"))
                .andExpect(status().isCreated());
    }

    private String createService(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/services")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId":"%s",
                                  "title":"Eletricidade residencial",
                                  "price":1500,
                                  "priceType":"FIXED"
                                }
                                """.formatted(CATEGORY_ID)))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }

    private String createBooking(String token, String serviceId, int days) throws Exception {
        MvcResult result = mockMvc.perform(post("/bookings")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "serviceOfferingId":"%s",
                                  "scheduledAt":"%s",
                                  "address":"Maputo",
                                  "paymentMethod":"CASH"
                                }
                                """.formatted(
                                        serviceId,
                                        Instant.now().plus(days, ChronoUnit.DAYS)
                                )))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
