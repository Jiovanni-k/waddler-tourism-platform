package org.example.rest.eventreservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventReservationRepository extends JpaRepository<EventReservation, Long>,
        JpaSpecificationExecutor<EventReservation> {

    List<EventReservation> findByEvent_IdAndStatus(Long eventId, EventReservationStatus status);

    List<EventReservation> findByEvent_Id(Long eventId);
    boolean existsByEvent_IdAndStatus(Long eventId, EventReservationStatus status);
    Optional<EventReservation> findByReservationCode(String reservationCode);
    List<EventReservation> findByDifficultyLevel(DifficultyLevel difficultyLevel);
    List<EventReservation> findByAgeRestriction(AgeRestriction ageRestriction);
    List<EventReservation> findByParticipantsCountGreaterThanEqual(Integer minParticipants);

    @Query("SELECT COUNT(r) > 0 FROM EventReservation r " +
            "WHERE r.userId = :userId " +
            "AND r.event.id = :eventId " +
            "AND r.status IN (org.example.rest.eventreservation.EventReservationStatus.ATTENDED, " +
            "org.example.rest.eventreservation.EventReservationStatus.COMPLETED)")
    boolean existsAttendedReservationByUserAndEvent(
            @Param("userId") Long userId,
            @Param("eventId") Long eventId);

    @Query("SELECT COALESCE(SUM(r.participantsCount), 0) FROM EventReservation r " +
            "WHERE r.event.id = :eventId AND r.status <> org.example.rest.eventreservation.EventReservationStatus.CANCELLED")
    Integer countTotalParticipantsByEventId(@Param("eventId") Long eventId);
}