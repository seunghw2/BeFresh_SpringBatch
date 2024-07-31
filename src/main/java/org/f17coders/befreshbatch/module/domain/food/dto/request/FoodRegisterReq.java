package org.f17coders.befreshbatch.module.domain.food.dto.request;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record FoodRegisterReq(
    String name,
    LocalDate expirationDate,
    long ftypeId,
    String qrId
) {
}
