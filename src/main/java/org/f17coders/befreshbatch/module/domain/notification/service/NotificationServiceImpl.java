package org.f17coders.befreshbatch.module.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.f17coders.befreshbatch.module.domain.food.Food;
import org.f17coders.befreshbatch.module.domain.member.Member;
import org.f17coders.befreshbatch.module.domain.memberToken.MemberToken;
import org.f17coders.befreshbatch.module.domain.notification.repository.NotificationRepository;
import org.f17coders.befreshbatch.module.domain.refrigerator.Refrigerator;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public void sendExpireNotification(List<Food> foodList, String category) {
        String title = "";
        String body = "";

        for (Food food : foodList) {
            title = "[" + food.getName() + "] " + " 상태가 되었어요!";
            body = "[유통 기한 D+" + ChronoUnit.DAYS.between(food.getExpirationDate(), LocalDate.now())
                + "]";
            if (category.equals("danger")) {
                body += " 주의해서 먹으세요!";
            }

            long notificationId = saveMessage(food.getRefrigerator(), category, title, body);
            Member member = food.getRefrigerator().getMember();

            for (MemberToken memberToken : member.getMemberTokenSet()) {
                sendMessage(memberToken, title, body, category, notificationId);
            }
        }
    }

    private long saveMessage(Refrigerator refrigerator, String category, String title,
        String body) {
        org.f17coders.befreshbatch.module.domain.notification.Notification notification = org.f17coders.befreshbatch.module.domain.notification.Notification.createNotification(
            category, title, body, refrigerator);
        return notificationRepository.save(notification).getId();
    }

    private void sendMessage(MemberToken memberToken, String title, String body, String category,
        long notificationId) {
        Message message = Message.builder()
            .setToken(memberToken.getToken())
            .setNotification(Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build()
            )
            .putData("category", category)
            .putData("notificationId", String.valueOf(notificationId))
            .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("[FCM send] " + response);
        } catch (FirebaseMessagingException e) {
            log.info("[FCM except]" + e.getMessage());
        }
    }
}
