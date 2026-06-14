package org.example.rest.eventreservation;

import org.example.rest.event.Event;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EventReservationMapper {

    public EventReservation toEntity(EventReservationRequestDto dto, Event event) {
        EventReservation reservation = new EventReservation();
        reservation.setReservationCode(generateCode());
        reservation.setEvent(event);
        int participants = dto.getParticipantsCount() != null ? dto.getParticipantsCount() : 1;
        reservation.setParticipantsCount(participants);
        java.math.BigDecimal calculatedAmount = java.math.BigDecimal.valueOf(event.getPrice())
                .multiply(java.math.BigDecimal.valueOf(participants));
        reservation.setTotalAmount(calculatedAmount);
        reservation.setCurrency(event.getCurrency() != null ? event.getCurrency() : "USD");
        reservation.setAgeRestriction(dto.getAgeRestriction() != null ? dto.getAgeRestriction() : AgeRestriction.ALL_AGES);
        reservation.setMinAge(dto.getMinAge());
        reservation.setMeetingPoint(dto.getMeetingPoint());
        reservation.setSpecialRequests(dto.getSpecialRequests());
        reservation.setStatus(EventReservationStatus.PENDING);
        return reservation;
    }

    public EventReservationResponseDto toResponseDto(EventReservation reservation) {
        EventReservationResponseDto dto = new EventReservationResponseDto();
        dto.setId(reservation.getId());
        dto.setReservationCode(reservation.getReservationCode());
        dto.setEventId(reservation.getEvent().getId());
        dto.setEventTitle(reservation.getEvent().getTitle());
        dto.setEventCategory(reservation.getEvent().getCategory());
        dto.setParticipantsCount(reservation.getParticipantsCount());
        dto.setTotalAmount(reservation.getTotalAmount());
        dto.setCurrency(reservation.getCurrency());
        dto.setDiscountPercentage(reservation.getDiscountPercentage());
        dto.setDifficultyLevel(reservation.getDifficultyLevel());
        dto.setAgeRestriction(reservation.getAgeRestriction());
        dto.setMinAge(reservation.getMinAge());
        dto.setMeetingPoint(reservation.getMeetingPoint());
        dto.setSpecialRequests(reservation.getSpecialRequests());
        dto.setStatus(reservation.getStatus());
        dto.setCheckedInAt(reservation.getCheckedInAt());
        dto.setCancelledAt(reservation.getCancelledAt());
        dto.setCancellationReason(reservation.getCancellationReason());
        dto.setCreatedAt(reservation.getCreatedAt());
        dto.setUpdatedAt(reservation.getUpdatedAt());
        return dto;
    }

    private String generateCode() {
        return "ER-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}