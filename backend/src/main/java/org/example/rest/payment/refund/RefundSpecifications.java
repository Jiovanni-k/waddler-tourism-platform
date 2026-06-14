package org.example.rest.payment.refund;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class RefundSpecifications {

    private RefundSpecifications() {}

    public static Specification<Refund> hasPaymentId(Long paymentId) {
        return (root, query, cb) ->
                paymentId == null ? null
                        : cb.equal(root.join("payment", JoinType.INNER).get("id"), paymentId);
    }

    public static Specification<Refund> hasUserId(Long userId) {
        return (root, query, cb) ->
                userId == null ? null
                        : cb.equal(root.join("payment", JoinType.INNER).get("userId"), userId);
    }

    public static Specification<Refund> hasHotelIdIn(List<Long> hotelIds) {
        return (root, query, cb) -> {
            if (hotelIds == null || hotelIds.isEmpty()) return cb.disjunction();
            return root.join("payment", JoinType.INNER)
                    .join("booking", JoinType.INNER)
                    .join("room", JoinType.INNER)
                    .join("hotel", JoinType.INNER)
                    .get("id").in(hotelIds);
        };
    }

    public static Specification<Refund> hasStatus(RefundStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Refund> createdAfter(LocalDateTime dateFrom) {
        return (root, query, cb) ->
                dateFrom == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), dateFrom);
    }

    public static Specification<Refund> createdBefore(LocalDateTime dateTo) {
        return (root, query, cb) ->
                dateTo == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), dateTo);
    }

    public static Specification<Refund> minAmount(BigDecimal minAmount) {
        return (root, query, cb) ->
                minAmount == null ? null : cb.greaterThanOrEqualTo(root.get("amount"), minAmount);
    }

    public static Specification<Refund> maxAmount(BigDecimal maxAmount) {
        return (root, query, cb) ->
                maxAmount == null ? null : cb.lessThanOrEqualTo(root.get("amount"), maxAmount);
    }
}