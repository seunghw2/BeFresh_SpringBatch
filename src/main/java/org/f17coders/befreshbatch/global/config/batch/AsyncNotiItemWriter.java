package org.f17coders.befreshbatch.global.config.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.f17coders.befreshbatch.module.domain.memberToken.MemberToken;
import org.f17coders.befreshbatch.module.domain.notification.Notification;
import org.f17coders.befreshbatch.module.domain.notification.service.NotificationService;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncNotiItemWriter implements ItemWriter<Notification> {

    private final NotificationService notificationService;
    private final DataSource dataSource;

    @Override
    public void write(Chunk<? extends Notification> notifications) throws Exception {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Notification notification : notifications) {
            Set<MemberToken> memberTokenSet = notification.getRefrigerator().getMember().getMemberTokenSet();

            for (MemberToken memberToken : memberTokenSet) {
                // 비동기 메서드 호출 및 futures에 추가
                CompletableFuture<Void> future =notificationService.sendMessage(notification, memberToken);
                futures.add(future);
            }
        }

        // 모든 비동기 작업 완료 대기
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 모든 알림 전송이 끝난 후 배치 삽입
        insertNotifications(notifications);
    }


    private void insertNotifications(Chunk<? extends Notification> notifications) throws Exception {
        JdbcBatchItemWriter<Notification> writer = new JdbcBatchItemWriterBuilder<Notification>()
            .sql("INSERT INTO notification (category, title, message, refrigerator_id, is_sent) VALUES (:category, :title, :message, :refrigerator.id, :isSent)")
            .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
            .dataSource(dataSource)
            .build();

        // Writer 초기화
        writer.afterPropertiesSet();

        // 배치 삽입 실행
        writer.write(notifications);
    }
}
