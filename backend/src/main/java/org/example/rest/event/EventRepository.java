package org.example.rest.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.targetId = :eventId AND r.targetType = 'EVENT'")
    Double getAvgRating(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.targetId = :eventId AND r.targetType = 'EVENT'")
    Long getReviewsCount(@Param("eventId") Long eventId);

    java.util.List<Event> findByHotel_IdAndStatus(Long hotelId, EventStatus status);
}