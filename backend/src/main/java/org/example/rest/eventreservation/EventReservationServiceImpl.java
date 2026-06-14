package org.example.rest.eventreservation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rest.PagedResponse;
import org.example.rest.event.Event;
import org.example.rest.event.EventCategory;
import org.example.rest.event.EventNotFoundException;
import org.example.rest.event.EventRepository;
import org.example.rest.event.EventStatus;
import org.example.rest.hotel.HotelRepository;
import org.example.rest.security.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventReservationServiceImpl implements EventReservationService {

    private final EventReservationRepository repository;
    private final EventReservationMapper mapper;
    private final EventRepository eventRepository;
    private final HotelRepository hotelRepository;

    @Override
    @Transactional
    public EventReservationResponseDto create(Long eventId, Long userId, EventReservationRequestDto dto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (event.getStatus() != EventStatus.PUBLISHED)
            throw new EventReservationForbiddenActionException(
                    "Reservations can only be made for published events");

        int requested = dto.getParticipantsCount() != null ? dto.getParticipantsCount() : 1;

        if (requested > event.getMaxPerUser())
            throw new EventReservationForbiddenActionException(
                    "Maximum participants per reservation for this event is " + event.getMaxPerUser());

        Integer currentParticipants = repository.countTotalParticipantsByEventId(eventId);
        if (currentParticipants + requested > event.getCapacityTotal())
            throw new EventReservationForbiddenActionException(
                    "Not enough capacity. Available: "
                            + (event.getCapacityTotal() - currentParticipants)
                            + ", requested: " + requested);

        EventReservation reservation = mapper.toEntity(dto, event);
        reservation.setUserId(userId);

        if (!event.getRequiresApproval())
            reservation.setStatus(EventReservationStatus.CONFIRMED);

        EventReservation saved = repository.save(reservation);
        log.info("Created event reservation id={} for eventId={} by userId={}", saved.getId(), eventId, userId);
        return mapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EventReservationResponseDto getById(Long eventId, Long id, Long currentUserId, String role) {
        if (!eventRepository.existsById(eventId))
            throw new EventNotFoundException(eventId);
        EventReservation reservation = repository.findById(id)
                .orElseThrow(() -> new EventReservationNotFoundException(id));
        if (!reservation.getEvent().getId().equals(eventId))
            throw new EventReservationNotFoundException(id);

        if ("USER".equals(role) && !reservation.getUserId().equals(currentUserId))
            throw new AccessDeniedException("You can only view your own reservations");

        return mapper.toResponseDto(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<EventReservationResponseDto> list(
            Long eventId, Long userId,
            EventReservationStatus status, EventCategory eventCategory,
            DifficultyLevel difficultyLevel, AgeRestriction ageRestriction,
            Integer minParticipants, Integer maxParticipants,
            BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable) {

        if (!eventRepository.existsById(eventId))
            throw new EventNotFoundException(eventId);

        if (minParticipants != null && maxParticipants != null && minParticipants > maxParticipants)
            throw new IllegalArgumentException("minParticipants must not be greater than maxParticipants");

        Specification<EventReservation> spec = Specification
                .where(EventReservationSpecification.hasEventId(eventId))
                .and(EventReservationSpecification.hasUserId(userId))
                .and(EventReservationSpecification.hasStatus(status))
                .and(EventReservationSpecification.hasEventCategory(eventCategory))
                .and(EventReservationSpecification.hasDifficultyLevel(difficultyLevel))
                .and(EventReservationSpecification.hasAgeRestriction(ageRestriction))
                .and(EventReservationSpecification.minParticipants(minParticipants))
                .and(EventReservationSpecification.maxParticipants(maxParticipants))
                .and(EventReservationSpecification.minTotalAmount(minAmount))
                .and(EventReservationSpecification.maxTotalAmount(maxAmount));

        return toPagedResponse(repository.findAll(spec, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<EventReservationResponseDto> listByHotelIds(
            Long eventId, List<Long> hotelIds,
            EventReservationStatus status, EventCategory eventCategory,
            DifficultyLevel difficultyLevel, AgeRestriction ageRestriction,
            Integer minParticipants, Integer maxParticipants,
            BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable) {

        if (!eventRepository.existsById(eventId))
            throw new EventNotFoundException(eventId);

        Specification<EventReservation> spec = Specification
                .where(EventReservationSpecification.hasEventId(eventId))
                .and(EventReservationSpecification.hasHotelIdIn(hotelIds))
                .and(EventReservationSpecification.hasStatus(status))
                .and(EventReservationSpecification.hasEventCategory(eventCategory))
                .and(EventReservationSpecification.hasDifficultyLevel(difficultyLevel))
                .and(EventReservationSpecification.hasAgeRestriction(ageRestriction))
                .and(EventReservationSpecification.minParticipants(minParticipants))
                .and(EventReservationSpecification.maxParticipants(maxParticipants))
                .and(EventReservationSpecification.minTotalAmount(minAmount))
                .and(EventReservationSpecification.maxTotalAmount(maxAmount));

        return toPagedResponse(repository.findAll(spec, pageable));
    }

    @Override
    @Transactional
    public EventReservationResponseDto update(Long eventId, Long id, Long managerId, EventReservationRequestDto dto) {
        EventReservation reservation = findAndValidate(eventId, id);
        verifyManagerOwnership(reservation.getEvent(), managerId);

        if (reservation.getStatus() == EventReservationStatus.CANCELLED
                || reservation.getStatus() == EventReservationStatus.COMPLETED
                || reservation.getStatus() == EventReservationStatus.ATTENDED)
            throw new EventReservationForbiddenActionException(
                    "Cannot update a reservation with status: " + reservation.getStatus().getDisplayName());

        if (dto.getParticipantsCount() != null
                && !dto.getParticipantsCount().equals(reservation.getParticipantsCount())) {
            Event event = reservation.getEvent();
            Integer currentParticipants = repository.countTotalParticipantsByEventId(eventId);
            int availableAfterRemovingCurrent = currentParticipants - reservation.getParticipantsCount();
            if (availableAfterRemovingCurrent + dto.getParticipantsCount() > event.getCapacityTotal())
                throw new EventReservationForbiddenActionException(
                        "Not enough capacity for the updated participants count");
        }

        int updatedParticipants = dto.getParticipantsCount() != null
                ? dto.getParticipantsCount() : reservation.getParticipantsCount();
        reservation.setParticipantsCount(updatedParticipants);

        java.math.BigDecimal recalculated = java.math.BigDecimal.valueOf(reservation.getEvent().getPrice())
                .multiply(java.math.BigDecimal.valueOf(updatedParticipants));
        reservation.setTotalAmount(recalculated);
        reservation.setCurrency(reservation.getEvent().getCurrency() != null
                ? reservation.getEvent().getCurrency() : "USD");
        reservation.setAgeRestriction(dto.getAgeRestriction() != null ? dto.getAgeRestriction() : reservation.getAgeRestriction());
        reservation.setMinAge(dto.getMinAge());
        reservation.setMeetingPoint(dto.getMeetingPoint());
        reservation.setSpecialRequests(dto.getSpecialRequests());

        log.info("Updated event reservation id={} by managerId={}", id, managerId);
        return mapper.toResponseDto(repository.save(reservation));
    }

    @Override
    @Transactional
    public EventReservationResponseDto confirm(Long eventId, Long id, Long managerId) {
        EventReservation reservation = findAndValidate(eventId, id);
        verifyManagerOwnership(reservation.getEvent(), managerId);
        if (reservation.getStatus() != EventReservationStatus.PENDING)
            throw new EventReservationForbiddenActionException("Only pending reservations can be confirmed");
        reservation.setStatus(EventReservationStatus.CONFIRMED);
        log.info("Confirmed event reservation id={} by managerId={}", id, managerId);
        return mapper.toResponseDto(repository.save(reservation));
    }

    @Override
    @Transactional
    public EventReservationResponseDto cancel(Long eventId, Long id, Long currentUserId, String cancellationReason) {
        EventReservation reservation = findAndValidate(eventId, id);
        String role = SecurityUtil.getCurrentUserRole();

        if ("USER".equals(role) && !reservation.getUserId().equals(currentUserId))
            throw new AccessDeniedException("You can only cancel your own reservations");

        if ("HOTEL_MANAGER".equals(role))
            verifyManagerOwnership(reservation.getEvent(), currentUserId);

        if (reservation.getStatus() == EventReservationStatus.COMPLETED
                || reservation.getStatus() == EventReservationStatus.ATTENDED)
            throw new EventReservationForbiddenActionException(
                    "Cannot cancel a reservation with status: " + reservation.getStatus().getDisplayName());
        if (reservation.getStatus() == EventReservationStatus.CANCELLED)
            throw new EventReservationForbiddenActionException("Reservation is already cancelled");

        if ("USER".equals(role) && reservation.getEvent().getStartDateTime() != null) {
            long minutesUntilStart = ChronoUnit.MINUTES.between(
                    LocalDateTime.now(), reservation.getEvent().getStartDateTime());
            if (minutesUntilStart < 60)
                throw new EventReservationForbiddenActionException(
                        "Cancellation is not allowed within 1 hour of the event start time. " +
                                "The event starts in " + minutesUntilStart + " minute(s).");
        }

        reservation.setStatus(EventReservationStatus.CANCELLED);
        reservation.setCancelledAt(LocalDateTime.now());
        reservation.setCancellationReason(cancellationReason);
        log.info("Cancelled event reservation id={} by userId={}", id, currentUserId);
        return mapper.toResponseDto(repository.save(reservation));
    }

    @Override
    @Transactional
    public EventReservationResponseDto complete(Long eventId, Long id, Long managerId) {
        EventReservation reservation = findAndValidate(eventId, id);
        verifyManagerOwnership(reservation.getEvent(), managerId);
        if (reservation.getStatus() != EventReservationStatus.CONFIRMED
                && reservation.getStatus() != EventReservationStatus.ATTENDED)
            throw new EventReservationForbiddenActionException(
                    "Only confirmed or attended reservations can be completed");
        reservation.setStatus(EventReservationStatus.COMPLETED);
        log.info("Completed event reservation id={} by managerId={}", id, managerId);
        return mapper.toResponseDto(repository.save(reservation));
    }

    @Override
    @Transactional
    public EventReservationResponseDto attend(Long eventId, Long id, Long managerId) {
        EventReservation reservation = findAndValidate(eventId, id);
        verifyManagerOwnership(reservation.getEvent(), managerId);
        if (reservation.getStatus() != EventReservationStatus.CONFIRMED)
            throw new EventReservationForbiddenActionException(
                    "Only confirmed reservations can be marked as attended");
        reservation.setStatus(EventReservationStatus.ATTENDED);
        reservation.setCheckedInAt(LocalDateTime.now());
        log.info("Marked event reservation id={} as attended by managerId={}", id, managerId);
        return mapper.toResponseDto(repository.save(reservation));
    }

    @Override
    @Transactional
    public EventReservationResponseDto noShow(Long eventId, Long id, Long managerId) {
        EventReservation reservation = findAndValidate(eventId, id);
        verifyManagerOwnership(reservation.getEvent(), managerId);
        if (reservation.getStatus() != EventReservationStatus.CONFIRMED)
            throw new EventReservationForbiddenActionException(
                    "Only confirmed reservations can be marked as no-show");
        reservation.setStatus(EventReservationStatus.NO_SHOW);
        log.info("Marked event reservation id={} as no-show by managerId={}", id, managerId);
        return mapper.toResponseDto(repository.save(reservation));
    }

    @Override
    @Transactional
    public void delete(Long eventId, Long id) {
        EventReservation reservation = findAndValidate(eventId, id);
        repository.delete(reservation);
        log.info("Deleted event reservation id={}", id);
    }

    private EventReservation findAndValidate(Long eventId, Long id) {
        if (!eventRepository.existsById(eventId))
            throw new EventNotFoundException(eventId);
        EventReservation reservation = repository.findById(id)
                .orElseThrow(() -> new EventReservationNotFoundException(id));
        if (!reservation.getEvent().getId().equals(eventId))
            throw new EventReservationNotFoundException(id);
        return reservation;
    }

    private void verifyManagerOwnership(Event event, Long managerId) {
        String role = SecurityUtil.getCurrentUserRole();
        if ("ADMIN".equals(role)) return;
        if (!event.getHotel().getManagerId().equals(managerId))
            throw new AccessDeniedException(
                    "You do not have permission to manage reservations for this event");
    }

    private PagedResponse<EventReservationResponseDto> toPagedResponse(Page<EventReservation> page) {
        return new PagedResponse<>(
                page.map(mapper::toResponseDto).getContent(),
                page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isLast()
        );
    }
}