package org.f17coders.befreshbatch.module.domain.food.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.f17coders.befreshbatch.global.config.batch.BatchScheduleConfig;
import org.f17coders.befreshbatch.module.domain.food.dto.request.FoodRegisterReqList;
import org.f17coders.befreshbatch.module.domain.food.service.FoodService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/foods")
@RequiredArgsConstructor
@CrossOrigin("*")
@Slf4j
public class FoodController {

    private final FoodService foodService;
    private final BatchScheduleConfig batchScheduleConfig;

    @PostMapping
    public ResponseEntity<Integer> registerFood(
        @RequestBody FoodRegisterReqList foodRegisterReqList) {

        foodService.registerFood(foodRegisterReqList);

        return null;
    }

    @GetMapping("/batch/expire")
    public ResponseEntity<Long> processBatchExpire() {
        batchScheduleConfig.runExpiredFoodJob();

        return null;
    }
}
