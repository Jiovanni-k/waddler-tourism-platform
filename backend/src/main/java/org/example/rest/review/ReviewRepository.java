package org.example.rest.review;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> {

    @Query("SELECT r FROM Review r WHERE r.reviewer.id = :userId AND r.targetType = :type AND r.targetId = :targetId")
    Optional<Review> findByReviewerUserIdAndTargetTypeAndTargetId(
            @Param("userId") Long reviewerUserId,
            @Param("type") ReviewTargetType targetType,
            @Param("targetId") Long targetId);

    @Query("SELECT COUNT(r) > 0 FROM Review r WHERE r.reviewer.id = :userId AND r.targetType = :type AND r.targetId = :targetId")
    boolean existsByReviewerUserIdAndTargetTypeAndTargetId(
            @Param("userId") Long reviewerUserId,
            @Param("type") ReviewTargetType targetType,
            @Param("targetId") Long targetId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.targetType = :type AND r.targetId = :targetId AND r.status = :status")
    long countByTargetTypeAndTargetIdAndStatus(
            @Param("type") ReviewTargetType targetType,
            @Param("targetId") Long targetId,
            @Param("status") ReviewStatus status);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.targetType = :type AND r.targetId = :targetId AND r.status = :status AND r.rating = :rating")
    long countByTargetTypeAndTargetIdAndStatusAndRating(
            @Param("type") ReviewTargetType targetType,
            @Param("targetId") Long targetId,
            @Param("status") ReviewStatus status,
            @Param("rating") Integer rating);
}