package org.f17coders.befreshbatch.module.domain.notification.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
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
            writeFailureToFile(notification, memberToken, e);  // 실패 시 파일에 기록
            log.info("[FINISH SLEEP]" + e.getMessage());
            throw e;
        }
    }

    private void writeFailureToFile(Notification notification, MemberToken memberToken, Exception e) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("logs/notification_failures.log", true))) {
            writer.write("Failed to send notification:");
            writer.newLine();
            writer.write("MemberToken: " + memberToken.getToken());
            writer.newLine();
            writer.write("Notification:");
            writer.newLine();
            writer.write("  Title: " + notification.getTitle());
            writer.newLine();
            writer.write("  Body: " + notification.getMessage());
            writer.newLine();
            writer.write("  Category: " + notification.getCategory());
            writer.newLine();
            writer.write("  Refrigerator ID: " + notification.getRefrigerator().getId());
            writer.newLine();
            writer.write("Exception: " + e.getMessage());
            writer.newLine();
            writer.write("--------------------------------------------------");
            writer.newLine();
        } catch (IOException ex) {
            log.error("Error writing to failure log file: " + ex.getMessage());
        }
    }
}
