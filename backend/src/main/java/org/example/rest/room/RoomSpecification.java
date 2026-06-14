package org.example.rest.room;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class RoomSpecification {

    public static Specification<Room> hasNameContaining(String name) {
        return (root, query, cb) -> name == null ? null
                : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Room> hasRoomType(RoomType roomType) {
        return (root, query, cb) -> roomType == null ? null
                : cb.equal(root.get("roomType"), roomType);
    }

    public static Specification<Room> hasHotelId(Long hotelId) {
        return (root, query, cb) -> hotelId == null ? null
                : cb.equal(root.join("hotel", JoinType.INNER).get("id"), hotelId);
    }

    public static Specification<Room> isActive(Boolean active) {
        return (root, query, cb) -> active == null ? null
                : cb.equal(root.get("active"), active);
    }

    public static Specification<Room> hasBedType(String bedType) {
        return (root, query, cb) -> bedType == null ? null
                : cb.equal(root.get("bedType"), bedType);
    }

    public static Specification<Room> minCapacity(Integer minCapacity) {
        return (root, query, cb) -> minCapacity == null ? null
                : cb.greaterThanOrEqualTo(root.get("maxCapacity"), minCapacity);
    }

    public static Specification<Room> minPrice(BigDecimal minPrice) {
        return (root, query, cb) -> minPrice == null ? null
                : cb.greaterThanOrEqualTo(root.get("basePrice"), minPrice);
    }

    public static Specification<Room> maxPrice(BigDecimal maxPrice) {
        return (root, query, cb) -> maxPrice == null ? null
                : cb.lessThanOrEqualTo(root.get("basePrice"), maxPrice);
    }

    public static Specification<Room> hasCancellationPolicy(Long policyId) {
        return (root, query, cb) -> policyId == null ? null
                : cb.equal(root.get("cancellationPolicyId"), policyId);
    }
}