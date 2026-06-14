package org.example.rest.room;

import org.example.rest.room.Room;
import org.example.rest.room.RoomPricingStrategy;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class BudgetPricingStrategy implements RoomPricingStrategy {
    @Override
    public Specification<Room> apply(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("basePrice"), maxPrice);
    }
}