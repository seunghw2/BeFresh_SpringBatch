package org.f17coders.befreshbatch.module.domain.food.dto.request;

import java.util.List;
import lombok.Builder;

@Builder
public record FoodRegisterReqList (
    long refrigeratorId,
    List<FoodRegisterReq> foodList
){ }
