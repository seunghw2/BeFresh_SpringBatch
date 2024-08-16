package org.f17coders.befreshbatch.global.config.batch;

import org.f17coders.befreshbatch.module.domain.food.Food;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;
import org.springframework.stereotype.Component;

@Component
public class FoodItemWriteListener implements ItemWriteListener<Food> {
    @Override
    public void afterWrite(Chunk<? extends Food> foods) {
        System.out.println(">> afterWrite : "+ foods);
    }
}
