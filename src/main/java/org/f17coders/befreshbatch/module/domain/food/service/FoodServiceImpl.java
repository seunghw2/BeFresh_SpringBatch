package org.f17coders.befreshbatch.module.domain.food.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.f17coders.befreshbatch.module.domain.food.dto.request.FoodRegisterReq;
import org.f17coders.befreshbatch.module.domain.food.dto.request.FoodRegisterReqList;
import org.f17coders.befreshbatch.module.domain.refrigerator.Refrigerator;
import org.f17coders.befreshbatch.module.domain.refrigerator.repository.RefrigeratorRepository;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class FoodServiceImpl implements FoodService {
    private final RefrigeratorRepository refrigeratorRepository;

    @Override
    public void registerFood(FoodRegisterReqList foodRegisterReqList) {

        Optional<Refrigerator> refrigerator = refrigeratorRepository.findById(
            foodRegisterReqList.refrigeratorId());

        for (FoodRegisterReq foodRegisterReq : foodRegisterReqList.foodList()) {
//            asyncRegisterFood(refrigerator.get(), foodRegisterReq);
        }
    }
}
