package org.f17coders.befreshbatch.module.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.f17coders.befreshbatch.module.domain.memberToken.MemberToken;
import org.f17coders.befreshbatch.module.domain.notification.Notification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    @Override
    @Async
    public CompletableFuture<Void> sendMessage(Notification notification, MemberToken memberToken)
        throws FirebaseMessagingException, InterruptedException {
        Message message = Message.builder()
            .setToken(memberToken.getToken())
            .setNotification(com.google.firebase.messaging.Notification.builder()
                .setTitle(notification.getTitle())
                .setBody(notification.getMessage())
                .build()
            )
            .putData("category", notification.getCategory())
            .putData("notificationId", String.valueOf(notification.getId()))
            .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("[FCM send] " + response);
            return null;
        } catch (FirebaseMessagingException e) {

            log.info("[FINISH SLEEP]" + e.getMessage());
            throw e;
        }
    }

}
