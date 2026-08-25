package com.linkops.booking.controller;

import com.linkops.booking.dto.BookingResponse;
import com.linkops.booking.dto.CreateBookingRequest;
import com.linkops.booking.service.BookingService;
import com.linkops.security.AuthenticatedUser;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/bookings")
@Tag(name = "Pedidos e agendamentos")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @Operation(summary = "Criar um pedido de serviço", description = "Disponível para clientes.")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<BookingResponse> create(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateBookingRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.create(user.id(), request));
    }

    @GetMapping
    @Operation(summary = "Listar o histórico de pedidos do utilizador")
    public ResponseEntity<Page<BookingResponse>> history(
            @AuthenticationPrincipal AuthenticatedUser user,
            Pageable pageable
    ) {
        return ResponseEntity.ok(bookingService.history(user, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar um pedido")
    public ResponseEntity<BookingResponse> get(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(bookingService.get(id, user));
    }

    @PatchMapping("/{id}/accept")
    @Operation(summary = "Aceitar um pedido")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<BookingResponse> accept(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(bookingService.accept(id, user.id()));
    }

    @PatchMapping("/{id}/reject")
    @Operation(summary = "Rejeitar um pedido")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<BookingResponse> reject(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(bookingService.reject(id, user.id()));
    }

    @PatchMapping("/{id}/start")
    @Operation(summary = "Iniciar a execução do serviço")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<BookingResponse> start(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(bookingService.start(id, user.id()));
    }

    @PatchMapping("/{id}/complete")
    @Operation(summary = "Concluir o serviço")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<BookingResponse> complete(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(bookingService.complete(id, user.id()));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancelar um pedido quando permitido")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<BookingResponse> cancel(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(bookingService.cancel(id, user.id()));
    }

    @PatchMapping("/{id}/payment/paid")
    @Operation(summary = "Registar pagamento como concluído")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<BookingResponse> markPaymentAsPaid(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(bookingService.markPaymentAsPaid(id, user.id()));
    }

    @PatchMapping("/{id}/payment/not-confirmed")
    @Operation(summary = "Registar pagamento como não confirmado")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<BookingResponse> markPaymentAsNotConfirmed(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(
                bookingService.markPaymentAsNotConfirmed(id, user.id())
        );
    }
}
