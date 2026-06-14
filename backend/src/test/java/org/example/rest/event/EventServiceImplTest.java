package org.example.rest.event;

import org.example.rest.PagedResponse;
import org.example.rest.hotel.Hotel;
import org.example.rest.hotel.HotelNotFoundException;
import org.example.rest.hotel.HotelRepository;
import org.example.rest.security.user.AppUser;
import org.example.rest.security.user.UserRepository;
import org.example.rest.security.SecurityUtil;
import org.example.rest.eventreservation.EventReservationRepository;
import org.example.rest.eventreservation.EventReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventReservationRepository eventReservationRepository;

    @InjectMocks
    private EventServiceImpl service;

    private Hotel hotel;
    private AppUser creator;
    private Event event;
    private EventRequestDto requestDto;

    @BeforeEach
    void setUp() {
        hotel = Hotel.builder()
                .id(1L)
                .name("Test Hotel")
                .managerId(100L)
                .build();

        creator = new AppUser();
        creator.setId(100L);
        creator.setFirstName("John");
        creator.setLastName("Manager");
        creator.setEmail("manager@example.com");

        event = new Event();
        event.setId(1L);
        event.setHotel(hotel);
        event.setCreatedBy(creator);
        event.setTitle("Test Event");
        event.setDescription("Test event description");
        event.setCategory(EventCategory.CONFERENCE);
        event.setStartDateTime(LocalDateTime.now().plusDays(5));
        event.setEndDateTime(LocalDateTime.now().plusDays(6));
        event.setLocationType(EventLocationType.HOTEL);
        event.setAddress("123 Main St");
        event.setCity("New York");
        event.setPrice(100L);
        event.setCurrency("USD");
        event.setStatus(EventStatus.DRAFT);

        requestDto = new EventRequestDto();
        requestDto.setTitle("Test Event");
        requestDto.setDescription("Test event description");
        requestDto.setCategory(EventCategory.CONFERENCE);
        requestDto.setStartDateTime(LocalDateTime.now().plusDays(5));
        requestDto.setEndDateTime(LocalDateTime.now().plusDays(6));
        requestDto.setLocationType(EventLocationType.HOTEL);
        requestDto.setAddress("123 Main St");
        requestDto.setCity("New York");
        requestDto.setPrice(100L);
        requestDto.setCurrency("USD");
    }

    @Test
    void testCreateEvent_Success() {
        Long hotelId = 1L;
        Long createdBy = 100L;

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(userRepository.findById(createdBy)).thenReturn(Optional.of(creator));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(eventRepository.getAvgRating(1L)).thenReturn(null);
        when(eventRepository.getReviewsCount(1L)).thenReturn(0L);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            EventResponseDto result = service.createEvent(hotelId, createdBy, requestDto);

            assertNotNull(result);
            verify(hotelRepository, times(1)).findById(hotelId);
            verify(eventRepository, times(1)).save(any(Event.class));
        }
    }

    @Test
    void testCreateEvent_HotelNotFound() {
        Long hotelId = 999L;
        Long createdBy = 100L;

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

        assertThrows(HotelNotFoundException.class,
                () -> service.createEvent(hotelId, createdBy, requestDto));

        verify(eventRepository, never()).save(any());
    }

    @Test
    void testCreateEvent_UserNotFound() {
        Long hotelId = 1L;
        Long createdBy = 999L;

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(userRepository.findById(createdBy)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.createEvent(hotelId, createdBy, requestDto));

        verify(eventRepository, never()).save(any());
    }

    @Test
    void testCreateEvent_AccessDenied_NotOwner() {
        Long hotelId = 1L;
        Long managerId = 999L;

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            assertThrows(AccessDeniedException.class,
                    () -> service.createEvent(hotelId, managerId, requestDto));

            verify(eventRepository, never()).save(any());
        }
    }

    @Test
    void testCreateEvent_InvalidTime() {
        Long hotelId = 1L;
        Long createdBy = 100L;

        requestDto.setEndDateTime(LocalDateTime.now().minusDays(1));


        assertThrows(EventForbiddenActionException.class,
                () -> service.createEvent(hotelId, createdBy, requestDto));

        verify(eventRepository, never()).save(any());
    }

    @Test
    void testCreateEvent_AdminCanCreateForAnyHotel() {
        Long hotelId = 1L;
        Long createdBy = 999L;

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(userRepository.findById(createdBy)).thenReturn(Optional.of(creator));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(eventRepository.getAvgRating(1L)).thenReturn(null);
        when(eventRepository.getReviewsCount(1L)).thenReturn(0L);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("ADMIN");

            EventResponseDto result = service.createEvent(hotelId, createdBy, requestDto);

            assertNotNull(result);
            verify(eventRepository, times(1)).save(any(Event.class));
        }
    }

    @Test
    void testUpdateEvent_Success() {
        Long eventId = 1L;
        Long managerId = 100L;

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(eventRepository.getAvgRating(1L)).thenReturn(null);
        when(eventRepository.getReviewsCount(1L)).thenReturn(0L);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            EventResponseDto result = service.updateEvent(eventId, managerId, requestDto);

            assertNotNull(result);
            verify(eventRepository, times(1)).save(any(Event.class));
        }
    }

    @Test
    void testUpdateEvent_NotFound() {
        Long eventId = 999L;
        Long managerId = 100L;

        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(EventNotFoundException.class,
                () -> service.updateEvent(eventId, managerId, requestDto));

        verify(eventRepository, never()).save(any());
    }

    @Test
    void testUpdateEvent_AccessDenied() {
        Long eventId = 1L;
        Long managerId = 999L;

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            assertThrows(AccessDeniedException.class,
                    () -> service.updateEvent(eventId, managerId, requestDto));

            verify(eventRepository, never()).save(any());
        }
    }

    @Test
    void testUpdateEvent_CannotUpdateCancelledEvent() {
        Long eventId = 1L;
        Long managerId = 100L;

        event.setStatus(EventStatus.CANCELLED);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            assertThrows(EventForbiddenActionException.class,
                    () -> service.updateEvent(eventId, managerId, requestDto));

            verify(eventRepository, never()).save(any());
        }
    }

    @Test
    void testUpdateEvent_AdminCanUpdateAnyEvent() {
        Long eventId = 1L;
        Long managerId = 999L;

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(eventRepository.getAvgRating(1L)).thenReturn(null);
        when(eventRepository.getReviewsCount(1L)).thenReturn(0L);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("ADMIN");

            EventResponseDto result = service.updateEvent(eventId, managerId, requestDto);

            assertNotNull(result);
            verify(eventRepository, times(1)).save(any(Event.class));
        }
    }

    @Test
    void testPublish_Success() {
        Long eventId = 1L;
        Long managerId = 100L;

        event.setStatus(EventStatus.DRAFT);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(eventRepository.getAvgRating(1L)).thenReturn(null);
        when(eventRepository.getReviewsCount(1L)).thenReturn(0L);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            EventResponseDto result = service.publish(eventId, managerId);

            assertNotNull(result);
            verify(eventRepository, times(1)).save(any(Event.class));
        }
    }

    @Test
    void testPublish_CannotPublishNonDraftEvent() {
        Long eventId = 1L;
        Long managerId = 100L;

        event.setStatus(EventStatus.PUBLISHED);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            assertThrows(EventForbiddenActionException.class,
                    () -> service.publish(eventId, managerId));

            verify(eventRepository, never()).save(any());
        }
    }

    @Test
    void testPublish_NotFound() {
        Long eventId = 999L;
        Long managerId = 100L;

        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(EventNotFoundException.class,
                () -> service.publish(eventId, managerId));
    }

    @Test
    void testCancel_Success() {
        Long eventId = 1L;
        Long managerId = 100L;

        event.setStatus(EventStatus.PUBLISHED);

        Event cancelledEvent = new Event();
        cancelledEvent.setId(eventId);
        cancelledEvent.setHotel(hotel);
        cancelledEvent.setCreatedBy(creator);
        cancelledEvent.setTitle("Test Event");
        cancelledEvent.setCategory(EventCategory.CONFERENCE);
        cancelledEvent.setStartDateTime(LocalDateTime.now().plusDays(5));
        cancelledEvent.setEndDateTime(LocalDateTime.now().plusDays(5).plusHours(2));
        cancelledEvent.setLocationType(EventLocationType.HOTEL);
        cancelledEvent.setAddress("Test Address");
        cancelledEvent.setCity("Test City");
        cancelledEvent.setPrice(100L);
        cancelledEvent.setCurrency("USD");
        cancelledEvent.setStatus(EventStatus.CANCELLED);

        Event otherEvent = new Event();
        otherEvent.setId(2L);
        otherEvent.setHotel(hotel);
        otherEvent.setCreatedBy(creator);
        otherEvent.setTitle("Other Event");
        otherEvent.setCategory(EventCategory.WORKSHOP);
        otherEvent.setStatus(EventStatus.PUBLISHED);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(cancelledEvent);

        when(eventRepository.findByHotel_IdAndStatus(hotel.getId(), EventStatus.PUBLISHED))
                .thenReturn(List.of(otherEvent));

        when(eventReservationRepository.findByEvent_IdAndStatus(eventId, EventReservationStatus.CONFIRMED))
                .thenReturn(List.of());
        when(eventReservationRepository.findByEvent_IdAndStatus(eventId, EventReservationStatus.PENDING))
                .thenReturn(List.of());
        when(eventRepository.getAvgRating(eventId)).thenReturn(null);
        when(eventRepository.getReviewsCount(eventId)).thenReturn(0L);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            EventResponseDto result = service.cancel(eventId, managerId);

            assertNotNull(result);
            assertEquals(EventStatus.CANCELLED, cancelledEvent.getStatus());
            verify(eventRepository, times(1)).save(any(Event.class));
            verify(eventReservationRepository, times(1))
                    .findByEvent_IdAndStatus(eventId, EventReservationStatus.CONFIRMED);
            verify(eventReservationRepository, times(1))
                    .findByEvent_IdAndStatus(eventId, EventReservationStatus.PENDING);
        }
    }

    @Test
    void testCancel_CannotCancelNonPublishedEvent() {
        Long eventId = 1L;
        Long managerId = 100L;

        event.setStatus(EventStatus.DRAFT);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            assertThrows(EventForbiddenActionException.class,
                    () -> service.cancel(eventId, managerId));

            verify(eventRepository, never()).save(any());
        }
    }

    @Test
    void testCancel_NotFound() {
        Long eventId = 999L;
        Long managerId = 100L;

        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(EventNotFoundException.class,
                () -> service.cancel(eventId, managerId));
    }

    @Test
    void testDelete_Success() {
        Long eventId = 1L;
        Long managerId = 100L;

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            service.delete(eventId, managerId);

            verify(eventRepository, times(1)).delete(any(Event.class));
        }
    }

    @Test
    void testDelete_NotFound() {
        Long eventId = 999L;
        Long managerId = 100L;

        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(EventNotFoundException.class,
                () -> service.delete(eventId, managerId));

        verify(eventRepository, never()).delete(any(Event.class));
    }

    @Test
    void testDelete_AccessDenied() {
        Long eventId = 1L;
        Long managerId = 999L;

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            assertThrows(AccessDeniedException.class,
                    () -> service.delete(eventId, managerId));

            verify(eventRepository, never()).delete(any(Event.class));
        }
    }

    @Test
    void testGetById_Success() {
        Long eventId = 1L;

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventRepository.getAvgRating(1L)).thenReturn(4.5);
        when(eventRepository.getReviewsCount(1L)).thenReturn(10L);

        EventResponseDto result = service.getById(eventId);

        assertNotNull(result);
        verify(eventRepository, times(1)).findById(eventId);
    }

    @Test
    void testGetById_NotFound() {
        Long eventId = 999L;

        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(EventNotFoundException.class,
                () -> service.getById(eventId));
    }

    @Test
    void testList_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Event> page = new PageImpl<>(List.of(event), pageable, 1);

        when(eventRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        PagedResponse<EventResponseDto> result = service.list(
                null, null, null, null, null, null, null, null, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(eventRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testList_WithFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Event> page = new PageImpl<>(List.of(event), pageable, 1);

        when(hotelRepository.existsById(1L)).thenReturn(true);
        when(eventRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        PagedResponse<EventResponseDto> result = service.list(
                "New York",
                1L,
                EventCategory.CONFERENCE,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(10),
                50L,
                200L,
                "test",
                EventStatus.PUBLISHED,
                true,
                pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(eventRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testList_EmptyResult() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Event> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(eventRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

        PagedResponse<EventResponseDto> result = service.list(
                null, null, null, null, null, null, null, null, null, null, pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }
}
