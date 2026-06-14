package org.example.rest.review;

import org.springframework.data.jpa.domain.Specification;

public final class ReviewSpecifications {

    private ReviewSpecifications() {}

    public static Specification<Review> target(ReviewTargetType type, Long targetId) {
        return (root, query, cb) -> {
            if (type == null || targetId == null) return null;
            return cb.and(
                    cb.equal(root.get("targetType"), type),
                    cb.equal(root.get("targetId"), targetId)
            );
        };
    }

    public static Specification<Review> status(ReviewStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }
}