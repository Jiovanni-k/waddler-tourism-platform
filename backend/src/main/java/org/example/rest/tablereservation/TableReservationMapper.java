package org.example.rest.tablereservation;

import org.example.rest.hotel.Hotel;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TableReservationMapper {

    public TableReservation toEntity(TableReservationRequestDto dto, Hotel hotel) {
        TableReservation reservation = new TableReservation();
        reservation.setReservationCode(generateCode());
        reservation.setHotel(hotel);
        reservation.setGuestCount(dto.getGuestCount());
        reservation.setReservationDateTime(dto.getReservationDateTime());
        reservation.setDurationMinutes(dto.getDurationMinutes() != null ? dto.getDurationMinutes() : 120);
        reservation.setSpecialOccasion(dto.getSpecialOccasion());
        reservation.setTableNumber(dto.getTableNumber());
        reservation.setTableType(dto.getTableType());
        reservation.setPreOrderItems(dto.getPreOrderItems());
        reservation.setDietaryRestrictions(dto.getDietaryRestrictions());
        reservation.setStatus(TableReservationStatus.PENDING);
        return reservation;
    }

    public TableReservationResponseDto toResponseDto(TableReservation reservation) {
        TableReservationResponseDto dto = new TableReservationResponseDto();
        dto.setId(reservation.getId());
        dto.setReservationCode(reservation.getReservationCode());
        dto.setHotelId(reservation.getHotel().getId());
        dto.setHotelName(reservation.getHotel().getName());
        dto.setGuestCount(reservation.getGuestCount());
        dto.setReservationDateTime(reservation.getReservationDateTime());
        dto.setDurationMinutes(reservation.getDurationMinutes());
        dto.setSpecialOccasion(reservation.getSpecialOccasion());
        dto.setTableNumber(reservation.getTableNumber());
        dto.setTableType(reservation.getTableType());
        dto.setPreOrderItems(reservation.getPreOrderItems());
        dto.setDietaryRestrictions(reservation.getDietaryRestrictions());
        dto.setStatus(reservation.getStatus());
        dto.setCancelledAt(reservation.getCancelledAt());
        dto.setCreatedAt(reservation.getCreatedAt());
        dto.setUpdatedAt(reservation.getUpdatedAt());
        return dto;
    }

    private String generateCode() {
        return "TR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}