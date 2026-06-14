package org.example.rest.room;

import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class LuxuryPricingStrategy implements RoomPricingStrategy {
    @Override
    public Specification<Room> apply(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("basePrice"), minPrice);
    }
}
