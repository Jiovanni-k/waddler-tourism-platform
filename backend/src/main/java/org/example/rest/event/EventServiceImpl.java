package org.example.rest.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rest.ErrorMessages;
import org.example.rest.PagedResponse;
import org.example.rest.hotel.HotelNotFoundException;
import org.example.rest.hotel.HotelRepository;
import org.example.rest.security.user.UserRepository;
import org.example.rest.security.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.example.rest.eventreservation.EventReservation;
import org.example.rest.eventreservation.EventReservationRepository;
import org.example.rest.eventreservation.EventReservationStatus;
import org.example.rest.notification.EmailService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final EventReservationRepository eventReservationRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public EventResponseDto createEvent(Long hotelId, Long createdBy, EventRequestDto request) {
        validateTime(request.getStartDateTime(), request.getEndDateTime());

        var hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException(hotelId));

        String role = SecurityUtil.getCurrentUserRole();
        if ("HOTEL_MANAGER".equals(role) && !hotel.getManagerId().equals(createdBy)) {
            throw new AccessDeniedException("You do not manage hotel " + hotelId);
        }

        var creator = userRepository.findById(createdBy)
                .orElseThrow(() -> new IllegalArgumentException(
                        ErrorMessages.userNotFoundById(createdBy)));

        Event event = EventMapper.toEntity(request);
        event.setHotel(hotel);
        event.setCreatedBy(creator);

        Event saved = eventRepository.save(event);
        log.info("Created event id={} for hotelId={} by managerId={}", saved.getId(), hotelId, createdBy);
        return withRatings(saved);
    }

    @Override
    @Transactional
    public EventResponseDto updateEvent(Long eventId, Long managerId, EventRequestDto request) {
        Event event = findOrThrow(eventId);
        verifyOwnership(event, managerId);

        if (Objects.equals(event.getStatus(), EventStatus.CANCELLED)
                || Objects.equals(event.getStatus(), EventStatus.ENDED)
                || Objects.equals(event.getStatus(), EventStatus.ARCHIVED)) {
            throw new EventForbiddenActionException("Cannot update an event in status: " + event.getStatus());
        }

        if (Objects.nonNull(request.getStartDateTime()) && Objects.nonNull(request.getEndDateTime())) {
            validateTime(request.getStartDateTime(), request.getEndDateTime());
        }

        EventMapper.applyChanges(request, event);
        Event saved = eventRepository.save(event);
        log.info("Updated event id={} by managerId={}", eventId, managerId);
        return withRatings(saved);
    }

    @Override
    @Transactional
    public EventResponseDto publish(Long eventId, Long managerId) {
        Event event = findOrThrow(eventId);
        verifyOwnership(event, managerId);

        if (!Objects.equals(event.getStatus(), EventStatus.DRAFT)) {
            throw new EventForbiddenActionException("Only DRAFT events can be published");
        }

        event.setStatus(EventStatus.PUBLISHED);
        log.info("Published event id={} by managerId={}", eventId, managerId);
        return withRatings(eventRepository.save(event));
    }

    @Override
    @Transactional
    public EventResponseDto cancel(Long eventId, Long managerId) {
        Event event = findOrThrow(eventId);
        verifyOwnership(event, managerId);

        if (!Objects.equals(event.getStatus(), EventStatus.PUBLISHED)) {
            throw new EventForbiddenActionException("Only PUBLISHED events can be cancelled");
        }

        event.setStatus(EventStatus.CANCELLED);
        EventResponseDto result = withRatings(eventRepository.save(event));
        log.info("Cancelled event id={} by managerId={}", eventId, managerId);

        List<Event> otherEvents = getOtherPublishedEvents(event);

        List<EventReservation> activeReservations = new java.util.ArrayList<>(eventReservationRepository
                .findByEvent_IdAndStatus(eventId, EventReservationStatus.CONFIRMED));
        activeReservations.addAll(eventReservationRepository
                .findByEvent_IdAndStatus(eventId, EventReservationStatus.PENDING));

        activeReservations.forEach(reservation -> {
            reservation.setStatus(EventReservationStatus.CANCELLED);
            reservation.setCancelledAt(java.time.LocalDateTime.now());
            reservation.setCancellationReason("Event cancelled by hotel manager");
            eventReservationRepository.save(reservation);

            userRepository.findById(reservation.getUserId()).ifPresent(user -> {
                try {
                    emailService.sendEventCancelledByManager(
                            user.getEmail(), user.getFirstName(), event, otherEvents);
                } catch (Exception e) {
                    log.warn("Event cancellation email failed for userId={} — {}", reservation.getUserId(), e.getMessage());
                }

                if (reservation.getTotalAmount() != null
                        && reservation.getTotalAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
                    try {
                        emailService.sendManagerCancellationRefund(
                                user.getEmail(), user.getFirstName(),
                                "Event: " + event.getTitle(),
                                reservation.getTotalAmount());
                    } catch (Exception e) {
                        log.warn("Refund email failed for userId={} — {}", reservation.getUserId(), e.getMessage());
                    }
                }
            });
        });

        log.info("Notified {} users about cancellation of event id={}", activeReservations.size(), eventId);
        return result;
    }

    private List<Event> getOtherPublishedEvents(Event cancelledEvent) {
        return eventRepository
                .findByHotel_IdAndStatus(cancelledEvent.getHotel().getId(), EventStatus.PUBLISHED)
                .stream()
                .filter(e -> !e.getId().equals(cancelledEvent.getId()))
                .limit(3)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long eventId, Long managerId) {
        Event event = findOrThrow(eventId);
        verifyOwnership(event, managerId);
        eventRepository.delete(event);
        log.info("Deleted event id={} by managerId={}", eventId, managerId);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponseDto getById(Long eventId) {
        return withRatings(findOrThrow(eventId));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<EventResponseDto> list(String city, Long hotelId, EventCategory category,
                                                LocalDateTime dateFrom, LocalDateTime dateTo,
                                                Long minPrice, Long maxPrice,
                                                String q, EventStatus status,
                                                Boolean hasReservations,
                                                Pageable pageable) {

        if (hotelId != null && !hotelRepository.existsById(hotelId)) {
            throw new org.example.rest.hotel.HotelNotFoundException(hotelId);
        }

        Specification<Event> spec = Specification
                .where(EventSpecifications.hasCity(city))
                .and(EventSpecifications.hasHotelId(hotelId))
                .and(EventSpecifications.hasCategory(category))
                .and(EventSpecifications.startsAfter(dateFrom))
                .and(EventSpecifications.endsBefore(dateTo))
                .and(EventSpecifications.minPrice(minPrice))
                .and(EventSpecifications.maxPrice(maxPrice))
                .and(EventSpecifications.searchQ(q))
                .and(EventSpecifications.hasStatus(status))
                .and(EventSpecifications.hasReservations(hasReservations));

        Page<Event> page = eventRepository.findAll(spec, pageable);

        return new PagedResponse<>(
                page.map(this::withRatings).getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    private Event findOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
    }

    private void verifyOwnership(Event event, Long managerId) {
        String role = SecurityUtil.getCurrentUserRole();
        if ("ADMIN".equals(role)) return;

        if (Objects.isNull(event.getHotel())
                || !event.getHotel().getManagerId().equals(managerId)) {
            throw new AccessDeniedException(
                    "You do not have permission to modify event id=" + event.getId());
        }
    }

    private EventResponseDto withRatings(Event event) {
        Double avgRating    = eventRepository.getAvgRating(event.getId());
        Long   reviewsCount = eventRepository.getReviewsCount(event.getId());
        return EventMapper.toResponseWithRatings(event, avgRating, reviewsCount);
    }

    private void validateTime(LocalDateTime start, LocalDateTime end) {
        if (Objects.isNull(start) || Objects.isNull(end)) return;
        if (!end.isAfter(start)) {
            throw new EventForbiddenActionException("endDateTime must be after startDateTime");
        }
    }

    @Override
    @Transactional
    public void addPhotoToEvent(Long eventId, String photoUrl, Long managerId) {
        Event event = findOrThrow(eventId);
        verifyOwnership(event, managerId);
        event.getPhotos().add(photoUrl);
        eventRepository.save(event);
    }
}