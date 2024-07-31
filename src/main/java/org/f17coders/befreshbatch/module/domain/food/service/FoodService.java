package org.f17coders.befreshbatch.module.domain.food.service;

import org.f17coders.befreshbatch.module.domain.food.dto.request.FoodRegisterReqList;

public interface FoodService {
    void registerFood(FoodRegisterReqList foodRegisterReq);
}
