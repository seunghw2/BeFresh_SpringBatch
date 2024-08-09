package org.f17coders.befreshbatch.global.config.batch;

import org.f17coders.befreshbatch.module.domain.food.Food;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;
import org.springframework.stereotype.Component;

@Component
public class CustomItemWriteListener implements ItemWriteListener<Food> {
    @Override
    public void afterWrite(Chunk<? extends Food> foods) {
        System.out.println(">> afterWrite : "+ foods);
    }

//    @Override
//    public void onWriteError(Exception exception, List items) {
//        System.out.println(">> onWriteError : " + exception.getMessage());
//        System.out.println(">> onWriteError : " + items);
//    }
}
