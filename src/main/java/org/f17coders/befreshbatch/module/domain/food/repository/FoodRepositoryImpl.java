package org.f17coders.befreshbatch.module.domain.food.repository;

import static org.f17coders.befreshbatch.module.domain.food.QFood.food;
import static org.f17coders.befreshbatch.module.domain.member.QMember.member;
import static org.f17coders.befreshbatch.module.domain.memberToken.QMemberToken.memberToken;
import static org.f17coders.befreshbatch.module.domain.refrigerator.QRefrigerator.refrigerator;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import org.f17coders.befreshbatch.module.domain.food.Food;
import org.springframework.stereotype.Service;

@Service
public class FoodRepositoryImpl implements FoodRepositoryCustom {

    private final JPAQueryFactory queryFactroy;

    public FoodRepositoryImpl(EntityManager em) {
        this.queryFactroy = new JPAQueryFactory(em);
    }

    @Override
    public List<Long> findExpireFood(){
        return queryFactroy
                .select(food.id)
                .from(food)
                .where((food.expirationDate.before(LocalDate.now())))
                .fetch();
    }

    @Override
    public List<Food> findUpdateFood(List<Long> foodIdList) {
        return queryFactroy
                .selectFrom(food)
                .where(food.id.in(foodIdList))
                .fetch();
    }

    @Override
    public List<Food> findNotiFood(List<Long> foodIdList) {
        return queryFactroy
                .selectFrom(food)
                .where(food.id.in(foodIdList))
                .join(food.refrigerator, refrigerator).fetchJoin()
                .join(refrigerator.member, member).fetchJoin()
                .join(member.memberTokenSet, memberToken).fetchJoin()
                .fetch();
    }
}
