package com.linkops.booking.service;

import com.linkops.booking.domain.Booking;
import com.linkops.booking.dto.BookingResponse;
import com.linkops.booking.dto.CreateBookingRequest;
import com.linkops.booking.repository.BookingRepository;
import com.linkops.common.exception.BadRequestException;
import com.linkops.common.exception.ResourceNotFoundException;
import com.linkops.notification.service.NotificationService;
import com.linkops.provider.domain.ProviderStatus;
import com.linkops.security.AuthenticatedUser;
import com.linkops.service.domain.ServiceOffering;
import com.linkops.service.repository.ServiceOfferingRepository;
import com.linkops.user.domain.User;
import com.linkops.user.domain.UserRole;
import com.linkops.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final NotificationService notificationService;

    public BookingService(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            ServiceOfferingRepository serviceOfferingRepository,
            NotificationService notificationService
    ) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.serviceOfferingRepository = serviceOfferingRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public BookingResponse create(UUID clientId, CreateBookingRequest request) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizador não encontrado."));
        if (client.getRole() != UserRole.CLIENT) {
            throw new BadRequestException("Apenas clientes podem criar pedidos de serviço.");
        }

        ServiceOffering offering = serviceOfferingRepository.findPublicById(
                        request.serviceOfferingId(), ProviderStatus.ACTIVE
                )
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado."));

        Booking booking = new Booking(
                client,
                offering,
                request.scheduledAt(),
                request.address(),
                request.notes(),
                request.paymentMethod()
        );
        bookingRepository.save(booking);
        notificationService.bookingCreated(booking);
        return BookingResponse.from(booking);
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> history(AuthenticatedUser user, Pageable pageable) {
        Pageable safePageable = bookingPageable(pageable);
        Page<Booking> bookings = switch (user.role()) {
            case CLIENT -> bookingRepository.findAllByClient_Id(user.id(), safePageable);
            case PROVIDER -> bookingRepository.findAllByProvider_User_Id(user.id(), safePageable);
            case ADMIN -> bookingRepository.findAll(safePageable);
        };
        return bookings.map(BookingResponse::from);
    }

    @Transactional(readOnly = true)
    public BookingResponse get(UUID bookingId, AuthenticatedUser user) {
        Booking booking = findById(bookingId);
        ensureCanView(booking, user);
        return BookingResponse.from(booking);
    }

    @Transactional
    public BookingResponse accept(UUID bookingId, UUID providerUserId) {
        Booking booking = findProviderBooking(bookingId, providerUserId);
        booking.accept();
        notificationService.bookingAccepted(booking);
        return BookingResponse.from(booking);
    }

    @Transactional
    public BookingResponse reject(UUID bookingId, UUID providerUserId) {
        Booking booking = findProviderBooking(bookingId, providerUserId);
        booking.reject();
        notificationService.bookingRejected(booking);
        return BookingResponse.from(booking);
    }

    @Transactional
    public BookingResponse start(UUID bookingId, UUID providerUserId) {
        Booking booking = findProviderBooking(bookingId, providerUserId);
        booking.start();
        return BookingResponse.from(booking);
    }

    @Transactional
    public BookingResponse complete(UUID bookingId, UUID providerUserId) {
        Booking booking = findProviderBooking(bookingId, providerUserId);
        booking.complete();
        booking.getProvider().recordCompletedJob();
        notificationService.bookingCompleted(booking);
        return BookingResponse.from(booking);
    }

    @Transactional
    public BookingResponse cancel(UUID bookingId, UUID clientId) {
        Booking booking = findById(bookingId);
        if (!booking.getClient().getId().equals(clientId)) {
            throw notFound();
        }
        booking.cancelByClient();
        notificationService.bookingCancelled(booking);
        return BookingResponse.from(booking);
    }

    @Transactional
    public BookingResponse markPaymentAsPaid(UUID bookingId, UUID providerUserId) {
        Booking booking = findProviderBooking(bookingId, providerUserId);
        booking.markPaymentAsPaid();
        return BookingResponse.from(booking);
    }

    @Transactional
    public BookingResponse markPaymentAsNotConfirmed(UUID bookingId, UUID providerUserId) {
        Booking booking = findProviderBooking(bookingId, providerUserId);
        booking.markPaymentAsNotConfirmed();
        return BookingResponse.from(booking);
    }

    private Booking findProviderBooking(UUID bookingId, UUID providerUserId) {
        Booking booking = findById(bookingId);
        if (!booking.getProvider().getUser().getId().equals(providerUserId)) {
            throw notFound();
        }
        return booking;
    }

    private Booking findById(UUID bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow(this::notFound);
    }

    private void ensureCanView(Booking booking, AuthenticatedUser user) {
        boolean involved = booking.getClient().getId().equals(user.id())
                || booking.getProvider().getUser().getId().equals(user.id());
        if (!involved && user.role() != UserRole.ADMIN) {
            throw notFound();
        }
    }

    private ResourceNotFoundException notFound() {
        return new ResourceNotFoundException("Pedido não encontrado.");
    }

    private Pageable bookingPageable(Pageable pageable) {
        Map<String, String> allowed = Map.of(
                "createdAt", "createdAt",
                "updatedAt", "updatedAt",
                "scheduledAt", "scheduledAt",
                "status", "status"
        );
        List<Sort.Order> orders = pageable.getSort().stream()
                .map(order -> {
                    String property = allowed.get(order.getProperty());
                    if (property == null) {
                        throw new BadRequestException(
                                "Ordenação inválida. Use createdAt, updatedAt, scheduledAt ou status."
                        );
                    }
                    return new Sort.Order(order.getDirection(), property);
                })
                .toList();
        Sort sort = orders.isEmpty()
                ? Sort.by(Sort.Order.desc("createdAt"))
                : Sort.by(orders);
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }
}
