package com.linkops.notification.domain;

import com.linkops.common.domain.BaseEntity;
import com.linkops.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "notifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationType type;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "read_at")
    private Instant readAt;

    public Notification(
            User recipient,
            NotificationType type,
            String title,
            String message,
            UUID referenceId
    ) {
        this.recipient = recipient;
        this.type = type;
        this.title = title;
        this.message = message;
        this.referenceId = referenceId;
    }

    public void markAsRead() {
        if (readAt == null) {
            readAt = Instant.now();
        }
    }

    public boolean isRead() {
        return readAt != null;
    }
}
