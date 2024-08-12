package org.f17coders.befreshbatch.global.config.batch;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.f17coders.befreshbatch.module.domain.memberToken.MemberToken;
import org.f17coders.befreshbatch.module.domain.notification.Notification;
import org.f17coders.befreshbatch.module.domain.notification.repository.NotificationRepository;
import org.f17coders.befreshbatch.module.domain.notification.service.NotificationService;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotiItemWriter implements ItemWriter<Notification> {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    public void write(Chunk<? extends Notification> notifications)
        throws FirebaseMessagingException {
        for (Notification notification : notifications) {
            Set<MemberToken> memberTokenSet = notification.getRefrigerator().getMember()
                .getMemberTokenSet();
            for (MemberToken memberToken : memberTokenSet) {
                try {
                    notificationService.sendMessage(notification, memberToken);
                } catch (FirebaseMessagingException e) {
                    if (e.getMessagingErrorCode().equals(MessagingErrorCode.INTERNAL)) {
                        throw e;
                    }
                }
            }
        }
        notificationRepository.saveAll(notifications);
    }
}
