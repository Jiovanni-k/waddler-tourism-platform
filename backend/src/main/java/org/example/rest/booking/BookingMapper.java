package org.example.rest.booking;

import org.example.rest.room.Room;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class BookingMapper {

    public Booking toEntity(BookingRequestDto dto, Long userId, Room room) {
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setRoom(room);
        booking.setCheckInDate(dto.getCheckInDate());
        booking.setCheckOutDate(dto.getCheckOutDate());
        booking.setNumberOfGuests(dto.getNumberOfGuests());
        booking.setBookingDate(LocalDateTime.now());
        booking.setStatus(BookingStatus.PENDING);
        return booking;
    }

    public BookingResponseDto toResponseDto(Booking booking) {
        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(booking.getId());

        Room room = booking.getRoom();
        if (room != null) {
            dto.setRoomId(room.getId());
            dto.setRoomName(room.getName());
            dto.setHotelId(room.getHotel() != null ? room.getHotel().getId() : null);
            dto.setHotelName(room.getHotel() != null ? room.getHotel().getName() : null);
        }

        dto.setCheckInDate(booking.getCheckInDate());
        dto.setCheckOutDate(booking.getCheckOutDate());
        dto.setNumberOfGuests(booking.getNumberOfGuests());
        dto.setBookingDate(booking.getBookingDate());
        dto.setStatus(booking.getStatus());
        dto.setTotalPrice(booking.getTotalPrice());

        dto.setCancellationPolicyName(booking.getCancellationPolicyName());
        dto.setCancellationPolicyDisplayName(
                booking.getCancellationPolicyName() != null
                        ? booking.getCancellationPolicyName().getDisplayName()
                        : null
        );
        dto.setCancellationPolicyDescription(booking.getCancellationPolicyDescription());
        dto.setCancellationDaysBeforeCheckin(booking.getCancellationDaysBeforeCheckin());
        dto.setCancellationRefundPercentage(booking.getCancellationRefundPercentage());
        dto.setRefundAmount(booking.getRefundAmount());

        dto.setCreatedAt(booking.getCreatedAt());
        dto.setUpdatedAt(booking.getUpdatedAt());
        return dto;
    }
}