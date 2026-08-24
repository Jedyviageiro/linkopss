package com.linkops.notification.repository;

import com.linkops.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findAllByRecipient_Id(UUID recipientId, Pageable pageable);

    Optional<Notification> findByIdAndRecipient_Id(UUID id, UUID recipientId);
}
