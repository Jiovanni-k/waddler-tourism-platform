package org.example.rest.event;

import org.example.rest.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface EventService {

    EventResponseDto createEvent(Long hotelId, Long createdBy, EventRequestDto request);

    EventResponseDto updateEvent(Long eventId, Long managerId, EventRequestDto request);

    EventResponseDto publish(Long eventId, Long managerId);

    EventResponseDto cancel(Long eventId, Long managerId);

    void delete(Long eventId, Long managerId);

    EventResponseDto getById(Long eventId);

    PagedResponse<EventResponseDto> list(
            String        city,
            Long          hotelId,
            EventCategory category,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Long          minPrice,
            Long          maxPrice,
            String        q,
            EventStatus   status,
            Boolean       hasReservations,
            Pageable      pageable
    );

    void addPhotoToEvent(Long eventId, String photoUrl, Long managerId);
}