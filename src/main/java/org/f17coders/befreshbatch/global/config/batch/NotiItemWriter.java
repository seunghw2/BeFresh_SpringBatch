package org.f17coders.befreshbatch.global.config.batch;

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

    public void write(Chunk<? extends Notification> notifications) throws Exception {
        notifications.forEach(notification -> {
            Set<MemberToken> memberTokenSet = notification.getRefrigerator().getMember()
                .getMemberTokenSet();
            memberTokenSet.forEach(memberToken -> notificationService.sendMessage(notification, memberToken));
        });

        notificationRepository.saveAll(notifications);  // TODO : 성공하는 경우에만 저장하도록 변경 필요
    }
}
