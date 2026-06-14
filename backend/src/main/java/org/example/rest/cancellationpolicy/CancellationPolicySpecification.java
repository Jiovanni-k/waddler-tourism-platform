package org.example.rest.cancellationpolicy;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Objects;

public class CancellationPolicySpecification {

    public static Specification<CancellationPolicy> hasHotelId(Long hotelId) {
        return (root, query, cb) -> Objects.isNull(hotelId) ? null
                : cb.equal(root.join("hotel", JoinType.INNER).get("id"), hotelId);
    }

    public static Specification<CancellationPolicy> hasName(CancellationPolicyName name) {
        return (root, query, cb) -> Objects.isNull(name) ? null
                : cb.equal(root.get("name"), name);
    }

    public static Specification<CancellationPolicy> minRefundPercentage(BigDecimal min) {
        return (root, query, cb) -> Objects.isNull(min) ? null
                : cb.greaterThanOrEqualTo(root.get("refundPercentage"), min);
    }

    public static Specification<CancellationPolicy> maxRefundPercentage(BigDecimal max) {
        return (root, query, cb) -> Objects.isNull(max) ? null
                : cb.lessThanOrEqualTo(root.get("refundPercentage"), max);
    }

    public static Specification<CancellationPolicy> minDaysBeforeCheckin(Integer min) {
        return (root, query, cb) -> Objects.isNull(min) ? null
                : cb.greaterThanOrEqualTo(root.get("daysBeforeCheckin"), min);
    }

    public static Specification<CancellationPolicy> maxDaysBeforeCheckin(Integer max) {
        return (root, query, cb) -> Objects.isNull(max) ? null
                : cb.lessThanOrEqualTo(root.get("daysBeforeCheckin"), max);
    }
}