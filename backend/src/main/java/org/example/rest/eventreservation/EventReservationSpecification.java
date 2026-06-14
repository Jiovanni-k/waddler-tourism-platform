package org.example.rest.eventreservation;

import jakarta.persistence.criteria.JoinType;
import org.example.rest.event.EventCategory;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

public class EventReservationSpecification {

    public static Specification<EventReservation> hasEventId(Long eventId) {
        return (root, query, cb) -> eventId == null ? null
                : cb.equal(root.join("event", JoinType.INNER).get("id"), eventId);
    }

    public static Specification<EventReservation> hasUserId(Long userId) {
        return (root, query, cb) -> userId == null ? null
                : cb.equal(root.get("userId"), userId);
    }

    public static Specification<EventReservation> hasStatus(EventReservationStatus status) {
        return (root, query, cb) -> status == null ? null
                : cb.equal(root.get("status"), status);
    }

    public static Specification<EventReservation> hasEventCategory(EventCategory category) {
        return (root, query, cb) -> category == null ? null
                : cb.equal(root.join("event", JoinType.INNER).get("category"), category);
    }

    public static Specification<EventReservation> hasDifficultyLevel(DifficultyLevel difficultyLevel) {
        return (root, query, cb) -> difficultyLevel == null ? null
                : cb.equal(root.get("difficultyLevel"), difficultyLevel);
    }

    public static Specification<EventReservation> hasAgeRestriction(AgeRestriction ageRestriction) {
        return (root, query, cb) -> ageRestriction == null ? null
                : cb.equal(root.get("ageRestriction"), ageRestriction);
    }

    public static Specification<EventReservation> minParticipants(Integer minParticipants) {
        return (root, query, cb) -> minParticipants == null ? null
                : cb.greaterThanOrEqualTo(root.get("participantsCount"), minParticipants);
    }

    public static Specification<EventReservation> maxParticipants(Integer maxParticipants) {
        return (root, query, cb) -> maxParticipants == null ? null
                : cb.lessThanOrEqualTo(root.get("participantsCount"), maxParticipants);
    }

    public static Specification<EventReservation> minTotalAmount(BigDecimal minAmount) {
        return (root, query, cb) -> minAmount == null ? null
                : cb.greaterThanOrEqualTo(root.get("totalAmount"), minAmount);
    }

    public static Specification<EventReservation> maxTotalAmount(BigDecimal maxAmount) {
        return (root, query, cb) -> maxAmount == null ? null
                : cb.lessThanOrEqualTo(root.get("totalAmount"), maxAmount);
    }

    public static Specification<EventReservation> hasHotelIdIn(List<Long> hotelIds) {
        return (root, query, cb) -> {
            if (hotelIds == null || hotelIds.isEmpty()) return cb.disjunction();
            return root.join("event", JoinType.INNER).join("hotel", JoinType.INNER).get("id").in(hotelIds);
        };
    }
}