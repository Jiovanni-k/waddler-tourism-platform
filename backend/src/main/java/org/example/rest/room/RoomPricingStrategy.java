
package org.example.rest.room;

import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;

public interface RoomPricingStrategy {
    Specification<Room> apply(BigDecimal minPrice, BigDecimal maxPrice);

}