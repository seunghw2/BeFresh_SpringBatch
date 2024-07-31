package org.f17coders.befreshbatch.module.domain.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.f17coders.befreshbatch.module.domain.refrigerator.Refrigerator;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id", nullable = false, length = 30)
    private Long id;

    @Column(nullable = false, length = 10)
    @Setter
    private String category;

    @Column(length = 300)
    private String title;

    @Column(nullable = false, length = 300)
    private String message;

    @ManyToOne
    @JoinColumn(name = "refrigerator_id", nullable = false)
    private Refrigerator refrigerator;

    public static Notification createNotification(String category, String title, String message, Refrigerator refrigerator) {
        Notification notification = new Notification();
        notification.setCategory(category);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRefrigerator(refrigerator);

        return notification;
    }
}
