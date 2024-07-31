package org.f17coders.befreshbatch.module.domain.notification.service;

import java.util.List;
import org.f17coders.befreshbatch.module.domain.food.Food;

public interface NotificationService {
    void sendExpireNotification(List<Food> foodList, String category);
}
