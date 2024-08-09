package org.f17coders.befreshbatch.module.domain.food.repository;

import org.f17coders.befreshbatch.module.domain.food.Food;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodRepository extends JpaRepository<Food, Long> {
}
