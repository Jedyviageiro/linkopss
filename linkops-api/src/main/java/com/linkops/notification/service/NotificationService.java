package com.linkops.notification.service;

import com.linkops.booking.domain.Booking;
import com.linkops.common.exception.BadRequestException;
import com.linkops.common.exception.ResourceNotFoundException;
import com.linkops.notification.domain.Notification;
import com.linkops.notification.domain.NotificationType;
import com.linkops.notification.dto.NotificationResponse;
import com.linkops.notification.repository.NotificationRepository;
import com.linkops.review.domain.Review;
import com.linkops.user.domain.User;
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
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void bookingCreated(Booking booking) {
        create(
                booking.getProvider().getUser(),
                NotificationType.BOOKING_CREATED,
                "Novo pedido de serviço",
                fullName(booking.getClient()) + " solicitou o serviço \""
                        + booking.getServiceOffering().getTitle() + "\".",
                booking.getId()
        );
    }

    @Transactional
    public void bookingAccepted(Booking booking) {
        createForClient(
                booking,
                NotificationType.BOOKING_ACCEPTED,
                "Pedido aceite",
                "O prestador aceitou o seu pedido para o serviço \""
                        + booking.getServiceOffering().getTitle() + "\"."
        );
    }

    @Transactional
    public void bookingRejected(Booking booking) {
        createForClient(
                booking,
                NotificationType.BOOKING_REJECTED,
                "Pedido rejeitado",
                "O prestador não pôde aceitar o seu pedido para o serviço \""
                        + booking.getServiceOffering().getTitle() + "\"."
        );
    }

    @Transactional
    public void bookingCancelled(Booking booking) {
        create(
                booking.getProvider().getUser(),
                NotificationType.BOOKING_CANCELLED,
                "Pedido cancelado",
                fullName(booking.getClient()) + " cancelou o pedido do serviço \""
                        + booking.getServiceOffering().getTitle() + "\".",
                booking.getId()
        );
    }

    @Transactional
    public void bookingCompleted(Booking booking) {
        createForClient(
                booking,
                NotificationType.BOOKING_COMPLETED,
                "Serviço concluído",
                "O serviço \"" + booking.getServiceOffering().getTitle()
                        + "\" foi marcado como concluído. Já pode deixar uma avaliação."
        );
    }

    @Transactional
    public void reviewReceived(Review review) {
        create(
                review.getProvider().getUser(),
                NotificationType.REVIEW_RECEIVED,
                "Nova avaliação recebida",
                fullName(review.getClient()) + " avaliou o seu serviço com "
                        + review.getRating() + " de 5 estrelas.",
                review.getId()
        );
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> list(UUID recipientId, Pageable pageable) {
        return notificationRepository
                .findAllByRecipient_Id(recipientId, safePageable(pageable))
                .map(NotificationResponse::from);
    }

    @Transactional
    public NotificationResponse markAsRead(UUID notificationId, UUID recipientId) {
        Notification notification = notificationRepository
                .findByIdAndRecipient_Id(notificationId, recipientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notificação não encontrada."
                ));
        notification.markAsRead();
        return NotificationResponse.from(notification);
    }

    private void createForClient(
            Booking booking,
            NotificationType type,
            String title,
            String message
    ) {
        create(booking.getClient(), type, title, message, booking.getId());
    }

    private void create(
            User recipient,
            NotificationType type,
            String title,
            String message,
            UUID referenceId
    ) {
        notificationRepository.save(
                new Notification(recipient, type, title, message, referenceId)
        );
    }

    private String fullName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }

    private Pageable safePageable(Pageable pageable) {
        Map<String, String> allowed = Map.of(
                "createdAt", "createdAt",
                "readAt", "readAt",
                "type", "type"
        );
        List<Sort.Order> orders = pageable.getSort().stream()
                .map(order -> {
                    String property = allowed.get(order.getProperty());
                    if (property == null) {
                        throw new BadRequestException(
                                "Ordenação inválida. Use createdAt, readAt ou type."
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
