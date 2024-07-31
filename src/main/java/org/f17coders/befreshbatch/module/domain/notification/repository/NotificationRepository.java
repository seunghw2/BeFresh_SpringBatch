package org.f17coders.befreshbatch.module.domain.notification.repository;

import org.f17coders.befreshbatch.module.domain.notification.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
