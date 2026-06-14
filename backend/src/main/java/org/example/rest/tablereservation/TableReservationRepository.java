package org.example.rest.tablereservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TableReservationRepository extends JpaRepository<TableReservation, Long>,
        JpaSpecificationExecutor<TableReservation> {

    Optional<TableReservation> findByReservationCode(String reservationCode);

    List<TableReservation> findByUserId(Long userId);
    List<TableReservation> findByHotelId(Long hotelId);
    List<TableReservation> findByHotelIdAndReservationDateTimeBetween(Long hotelId, LocalDateTime start, LocalDateTime end);
    List<TableReservation> findByHotelIdAndStatus(Long hotelId, TableReservationStatus status);
    boolean existsByHotelIdAndReservationDateTimeAndTableNumberAndStatusNot(Long hotelId, LocalDateTime reservationDateTime, String tableNumber, TableReservationStatus status);
    List<TableReservation> findBySpecialOccasionIsNotNull();
    List<TableReservation> findByHotelIdAndSpecialOccasion(Long hotelId, SpecialOccasion specialOccasion);
    boolean existsByHotelIdAndReservationDateTimeAndTableNumberAndStatusNotAndIdNot(
            Long hotelId, LocalDateTime reservationDateTime, String tableNumber,
            TableReservationStatus status, Long id);
}