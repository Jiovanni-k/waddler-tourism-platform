package org.example.rest.hotel;

import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Objects;

public final class HotelSpecifications {

    private HotelSpecifications() {
    }

    public static Specification<Hotel> hasStatus(HotelStatus status) {
        return (root, query, cb) ->
                Objects.isNull(status) ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Hotel> hasCity(String city) {
        return (root, query, cb) -> {
            if (Objects.isNull(city)) return null;
            return cb.equal(cb.lower(root.get("city")), city.toLowerCase());
        };
    }

    public static Specification<Hotel> hasMinStars(Integer minStars) {
        return (root, query, cb) ->
                Objects.isNull(minStars) ? null : cb.greaterThanOrEqualTo(root.get("starRating"), minStars);
    }

    public static Specification<Hotel> hasMinRating(BigDecimal minRating) {
        return (root, query, cb) ->
                Objects.isNull(minRating) ? null : cb.greaterThanOrEqualTo(root.get("averageGuestRating"), minRating);
    }

    public static Specification<Hotel> searchByNameOrCity(String q) {
        return (root, query, cb) -> {
            if (Objects.isNull(q)) return null;
            String like = "%" + q.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("city")), like)
            );
        };
    }
}