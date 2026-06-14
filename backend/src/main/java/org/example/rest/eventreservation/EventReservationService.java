package org.example.rest.eventreservation;

import org.example.rest.PagedResponse;
import org.example.rest.event.EventCategory;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface EventReservationService {

    EventReservationResponseDto create(Long eventId, Long userId, EventReservationRequestDto dto);

    EventReservationResponseDto getById(Long eventId, Long id, Long currentUserId, String role);

    PagedResponse<EventReservationResponseDto> list(
            Long eventId, Long userId,
            EventReservationStatus status, EventCategory eventCategory,
            DifficultyLevel difficultyLevel, AgeRestriction ageRestriction,
            Integer minParticipants, Integer maxParticipants,
            BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);

    PagedResponse<EventReservationResponseDto> listByHotelIds(
            Long eventId, List<Long> hotelIds,
            EventReservationStatus status, EventCategory eventCategory,
            DifficultyLevel difficultyLevel, AgeRestriction ageRestriction,
            Integer minParticipants, Integer maxParticipants,
            BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);

    EventReservationResponseDto update(Long eventId, Long id, Long managerId, EventReservationRequestDto dto);

    EventReservationResponseDto confirm(Long eventId, Long id, Long managerId);

    EventReservationResponseDto cancel(Long eventId, Long id, Long currentUserId, String cancellationReason);

    EventReservationResponseDto complete(Long eventId, Long id, Long managerId);

    EventReservationResponseDto attend(Long eventId, Long id, Long managerId);

    EventReservationResponseDto noShow(Long eventId, Long id, Long managerId);

    void delete(Long eventId, Long id);
}