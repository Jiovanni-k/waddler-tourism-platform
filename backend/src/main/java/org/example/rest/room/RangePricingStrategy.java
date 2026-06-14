package org.example.rest.room;

import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class RangePricingStrategy implements RoomPricingStrategy {

    @Override
    public Specification<Room> apply(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) ->
                cb.between(root.get("basePrice"), minPrice, maxPrice);
    }
}
