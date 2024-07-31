package org.f17coders.befreshbatch.module.domain.food.repository;

import org.f17coders.befreshbatch.module.domain.food.Food;
import java.util.List;

public interface FoodRepositoryCustom {
    List<Long> findDangerFood();

    List<Food> findUpdateFood(List<Long> foodIdList);

    List<Food> findNotiFood(List<Long> foodIdList);
}
