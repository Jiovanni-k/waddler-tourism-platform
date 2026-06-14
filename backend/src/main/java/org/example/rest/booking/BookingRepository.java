package org.example.rest.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>,
        JpaSpecificationExecutor<Booking> {

    boolean existsByRoom_Id(Long roomId);
    boolean existsByRoom_IdAndStatusIn(Long roomId, List<BookingStatus> statuses);
    List<Booking> findByRoom_Id(Long roomId);

    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.userId = :userId " +
            "AND b.room.hotel.id = :hotelId " +
            "AND b.status = org.example.rest.booking.BookingStatus.COMPLETED")
    boolean existsCompletedBookingByUserAndHotel(
            @Param("userId") Long userId,
            @Param("hotelId") Long hotelId);

}