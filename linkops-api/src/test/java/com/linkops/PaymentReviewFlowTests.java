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
class PaymentReviewFlowTests {

    private static final String ELECTRICIAN_CATEGORY =
            "11000000-0000-0000-0000-000000000001";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRecordPaymentAndReviewOnlyCompletedBookingOnce() throws Exception {
        ProviderData provider = createProvider();
        String serviceId = createService(provider.token());
        String clientToken = register(
                "payment.client@linkops.local", "Clara", "Cliente", "CLIENT"
        );
        String outsiderToken = register(
                "review.outsider@linkops.local", "Outro", "Cliente", "CLIENT"
        );
        String bookingId = createBooking(clientToken, serviceId);

        mockMvc.perform(patch("/bookings/{id}/payment/paid", bookingId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + provider.token()))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/bookings/{id}/review", bookingId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":5}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/bookings/{id}/accept", bookingId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + provider.token()))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/bookings/{id}/payment/not-confirmed", bookingId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + provider.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("NOT_CONFIRMED"));

        mockMvc.perform(patch("/bookings/{id}/payment/paid", bookingId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + provider.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentMethod").value("MPESA"))
                .andExpect(jsonPath("$.paymentMethodName").value("M-Pesa"))
                .andExpect(jsonPath("$.paymentStatus").value("PAID"))
                .andExpect(jsonPath("$.paymentStatusName").value("Pago"));

        mockMvc.perform(patch("/bookings/{id}/payment/not-confirmed", bookingId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + provider.token()))
                .andExpect(status().isConflict());

        mockMvc.perform(patch("/bookings/{id}/start", bookingId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + provider.token()))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/bookings/{id}/complete", bookingId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + provider.token()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/bookings/{id}/review", bookingId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + outsiderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":1}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/bookings/{id}/review", bookingId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rating": 5,
                                  "comment": "Excelente atendimento e trabalho." 
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.providerId").value(provider.profileId()));

        mockMvc.perform(post("/bookings/{id}/review", bookingId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":4}"))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/providers/{id}/reviews", provider.profileId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].rating").value(5));

        mockMvc.perform(get("/providers/{id}", provider.profileId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(5.0))
                .andExpect(jsonPath("$.completedJobs").value(1));
    }

    private ProviderData createProvider() throws Exception {
        String token = register(
                "payment.provider@linkops.local", "Paulo", "Prestador", "PROVIDER"
        );
        MvcResult result = mockMvc.perform(post("/providers/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"city\":\"Maputo\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return new ProviderData(
                token,
                JsonPath.read(result.getResponse().getContentAsString(), "$.id")
        );
    }

    private String register(
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
        return JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
    }

    private String createService(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/services")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId":"%s",
                                  "title":"Instalação elétrica",
                                  "price":1500,
                                  "priceType":"FIXED"
                                }
                                """.formatted(ELECTRICIAN_CATEGORY)))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }

    private String createBooking(String token, String serviceId) throws Exception {
        MvcResult result = mockMvc.perform(post("/bookings")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "serviceOfferingId":"%s",
                                  "scheduledAt":"%s",
                                  "address":"Maputo",
                                  "paymentMethod":"MPESA"
                                }
                                """.formatted(
                                        serviceId,
                                        Instant.now().plus(2, ChronoUnit.DAYS)
                                )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentMethodName").value("M-Pesa"))
                .andExpect(jsonPath("$.paymentStatus").value("PENDING"))
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }

    private record ProviderData(String token, String profileId) {
    }
}
