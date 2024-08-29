package org.f17coders.befreshbatch.module.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import java.util.concurrent.CompletableFuture;
import org.f17coders.befreshbatch.module.domain.memberToken.MemberToken;
import org.f17coders.befreshbatch.module.domain.notification.Notification;

public interface NotificationService {
    CompletableFuture<Void> sendMessage(Notification notification, MemberToken memberToken)
        throws FirebaseMessagingException, InterruptedException;
}
