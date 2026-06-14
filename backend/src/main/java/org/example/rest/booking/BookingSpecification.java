package org.example.rest.booking;

import jakarta.persistence.criteria.JoinType;
import org.example.rest.cancellationpolicy.CancellationPolicyName;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Objects;

public class    BookingSpecification {

    public static Specification<Booking> hasUserId(Long userId) {
        return (root, query, cb) -> Objects.isNull(userId) ? null
                : cb.equal(root.get("userId"), userId);
    }

    public static Specification<Booking> hasStatus(BookingStatus status) {
        return (root, query, cb) -> Objects.isNull(status) ? null
                : cb.equal(root.get("status"), status);
    }

    public static Specification<Booking> hasHotelId(Long hotelId) {
        return (root, query, cb) -> Objects.isNull(hotelId) ? null
                : cb.equal(root.join("room", JoinType.INNER).join("hotel", JoinType.INNER).get("id"), hotelId);
    }

    public static Specification<Booking> minPrice(Double minPrice) {
        return (root, query, cb) -> Objects.isNull(minPrice) ? null
                : cb.greaterThanOrEqualTo(root.get("totalPrice"), minPrice);
    }

    public static Specification<Booking> maxPrice(Double maxPrice) {
        return (root, query, cb) -> Objects.isNull(maxPrice) ? null
                : cb.lessThanOrEqualTo(root.get("totalPrice"), maxPrice);
    }

    public static Specification<Booking> hasHotelIdIn(List<Long> hotelIds) {
        return (root, query, cb) -> {
            if (hotelIds == null || hotelIds.isEmpty()) return cb.disjunction();
            return root.join("room", JoinType.INNER).join("hotel", JoinType.INNER).get("id").in(hotelIds);
        };
    }

    public static Specification<Booking> hasCancellationPolicyName(CancellationPolicyName policyName) {
        return (root, query, cb) -> Objects.isNull(policyName) ? null
                : cb.equal(root.get("cancellationPolicyName"), policyName);
    }
}