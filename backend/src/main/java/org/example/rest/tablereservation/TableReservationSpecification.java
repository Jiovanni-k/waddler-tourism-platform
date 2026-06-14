package org.example.rest.tablereservation;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class TableReservationSpecification {

    public static Specification<TableReservation> hasHotelId(Long hotelId) {
        return (root, query, cb) -> hotelId == null ? null
                : cb.equal(root.join("hotel", JoinType.INNER).get("id"), hotelId);
    }

    public static Specification<TableReservation> hasUserId(Long userId) {
        return (root, query, cb) -> userId == null ? null
                : cb.equal(root.get("userId"), userId);
    }

    public static Specification<TableReservation> hasStatus(TableReservationStatus status) {
        return (root, query, cb) -> status == null ? null
                : cb.equal(root.get("status"), status);
    }

    public static Specification<TableReservation> hasTableType(TableType tableType) {
        return (root, query, cb) -> tableType == null ? null
                : cb.equal(root.get("tableType"), tableType);
    }

    public static Specification<TableReservation> hasSpecialOccasion(SpecialOccasion specialOccasion) {
        return (root, query, cb) -> specialOccasion == null ? null
                : cb.equal(root.get("specialOccasion"), specialOccasion);
    }

    public static Specification<TableReservation> hasGuestCountGreaterThanOrEqual(Integer minGuests) {
        return (root, query, cb) -> minGuests == null ? null
                : cb.greaterThanOrEqualTo(root.get("guestCount"), minGuests);
    }

    public static Specification<TableReservation> hasGuestCountLessThanOrEqual(Integer maxGuests) {
        return (root, query, cb) -> maxGuests == null ? null
                : cb.lessThanOrEqualTo(root.get("guestCount"), maxGuests);
    }

    public static Specification<TableReservation> reservationAfter(LocalDateTime from) {
        return (root, query, cb) -> from == null ? null
                : cb.greaterThanOrEqualTo(root.get("reservationDateTime"), from);
    }

    public static Specification<TableReservation> reservationBefore(LocalDateTime to) {
        return (root, query, cb) -> to == null ? null
                : cb.lessThanOrEqualTo(root.get("reservationDateTime"), to);
    }

    public static Specification<TableReservation> hasTableNumber(String tableNumber) {
        return (root, query, cb) -> tableNumber == null ? null
                : cb.equal(root.get("tableNumber"), tableNumber);
    }
}