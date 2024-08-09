package org.f17coders.befreshbatch.global.config.batch;

import lombok.RequiredArgsConstructor;
import org.f17coders.befreshbatch.module.domain.food.Food;
import org.f17coders.befreshbatch.module.domain.food.repository.FoodRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomItemWriter implements ItemWriter<Food> {

    private final FoodRepository foodRepository;

    public void write(Chunk<? extends Food> foods) throws Exception {
        foodRepository.saveAllAndFlush(foods);
    }
}
