package org.example.rest.event;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.example.rest.eventreservation.EventReservation;

import java.time.LocalDateTime;
import java.util.Objects;

public final class EventSpecifications {

    private EventSpecifications() {}

    public static Specification<Event> hasCity(String city) {
        return (root, query, cb) ->
                Objects.isNull(city) || city.isBlank()
                        ? null
                        : cb.equal(cb.lower(root.get("city")), city.toLowerCase());
    }

    public static Specification<Event> hasHotelId(Long hotelId) {
        return (root, query, cb) ->
                Objects.isNull(hotelId) ? null : cb.equal(root.get("hotel").get("id"), hotelId);
    }

    public static Specification<Event> hasCategory(EventCategory category) {
        return (root, query, cb) ->
                Objects.isNull(category) ? null : cb.equal(root.get("category"), category);
    }

    public static Specification<Event> hasStatus(EventStatus status) {
        return (root, query, cb) ->
                Objects.isNull(status) ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Event> startsAfter(LocalDateTime dateFrom) {
        return (root, query, cb) ->
                Objects.isNull(dateFrom) ? null : cb.greaterThanOrEqualTo(root.get("startDateTime"), dateFrom);
    }

    public static Specification<Event> endsBefore(LocalDateTime dateTo) {
        return (root, query, cb) ->
                Objects.isNull(dateTo) ? null : cb.lessThanOrEqualTo(root.get("endDateTime"), dateTo);
    }

    public static Specification<Event> minPrice(Long minPrice) {
        return (root, query, cb) ->
                Objects.isNull(minPrice) ? null : cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Event> maxPrice(Long maxPrice) {
        return (root, query, cb) ->
                Objects.isNull(maxPrice) ? null : cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<Event> searchQ(String q) {
        return (root, query, cb) -> {
            if (Objects.isNull(q) || q.isBlank()) return null;
            String like = "%" + q.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")),       like),
                    cb.like(cb.lower(root.get("description")), like)
            );
        };
    }

    public static Specification<Event> hasReservations(Boolean hasReservations) {
        return (root, query, cb) -> {
            if (Objects.isNull(hasReservations)) return null;
            Join<Event, EventReservation> join = root.join("eventReservations", JoinType.LEFT);
            query.distinct(true);
            return hasReservations
                    ? cb.isNotNull(join.get("id"))
                    : cb.isNull(join.get("id"));
        };
    }
}