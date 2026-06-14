package org.example.rest.payment;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class PaymentSpecifications {

    private PaymentSpecifications() {}

    public static Specification<Payment> hasUserId(Long userId) {
        return (root, query, cb) ->
                userId == null ? null : cb.equal(root.get("userId"), userId);
    }

    public static Specification<Payment> hasBookingId(Long bookingId) {
        return (root, query, cb) ->
                bookingId == null ? null
                        : cb.equal(root.join("booking", JoinType.INNER).get("id"), bookingId);
    }

    public static Specification<Payment> hasHotelIdIn(List<Long> hotelIds) {
        return (root, query, cb) -> {
            if (hotelIds == null || hotelIds.isEmpty()) return cb.disjunction();
            return root.join("booking", JoinType.INNER)
                    .join("room", JoinType.INNER)
                    .join("hotel", JoinType.INNER)
                    .get("id").in(hotelIds);
        };
    }

    public static Specification<Payment> hasStatus(PaymentStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("paymentStatus"), status);
    }

    public static Specification<Payment> hasPaymentMethod(PaymentMethod method) {
        return (root, query, cb) ->
                method == null ? null : cb.equal(root.get("paymentMethod"), method);
    }

    public static Specification<Payment> isFlagged(Boolean flagged) {
        return (root, query, cb) ->
                flagged == null ? null : cb.equal(root.get("fraudDetectionFlag"), flagged);
    }

    public static Specification<Payment> createdAfter(LocalDateTime dateFrom) {
        return (root, query, cb) ->
                dateFrom == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), dateFrom);
    }

    public static Specification<Payment> createdBefore(LocalDateTime dateTo) {
        return (root, query, cb) ->
                dateTo == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), dateTo);
    }

    public static Specification<Payment> minAmount(BigDecimal minAmount) {
        return (root, query, cb) ->
                minAmount == null ? null : cb.greaterThanOrEqualTo(root.get("amount"), minAmount);
    }

    public static Specification<Payment> maxAmount(BigDecimal maxAmount) {
        return (root, query, cb) ->
                maxAmount == null ? null : cb.lessThanOrEqualTo(root.get("amount"), maxAmount);
    }
}