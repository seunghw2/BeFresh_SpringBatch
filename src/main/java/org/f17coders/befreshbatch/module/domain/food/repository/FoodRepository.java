package org.f17coders.befreshbatch.module.domain.food.repository;

import org.f17coders.befreshbatch.module.domain.food.Food;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodRepository extends JpaRepository<Food, Long>, FoodRepositoryCustom {

    List<Food> findByRefrigerator_Id(Long refrigeratorId);
}
