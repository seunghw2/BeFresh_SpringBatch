package org.f17coders.befreshbatch.global.config.batch;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.f17coders.befreshbatch.module.domain.food.Food;
import org.f17coders.befreshbatch.module.domain.food.repository.FoodRepository;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomItemWriter implements ItemWriter<Food> {

    private StepExecution stepExecution;
    private final FoodRepository foodRepository;

    public void write(Chunk<? extends Food> foods) throws Exception {
        foodRepository.saveAllAndFlush(foods);

        List<Long> foodIdList = new ArrayList<>();
        foods.forEach(food -> foodIdList.add(food.getId()));

        ExecutionContext stepContext = this.stepExecution.getExecutionContext();
        stepContext.put("foods", foodIdList);
    }

    @BeforeStep
    public void saveStepExecution(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }
}
