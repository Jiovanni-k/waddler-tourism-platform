package org.example.rest.eventreservation;

import org.example.rest.PagedResponse;
import org.example.rest.event.Event;
import org.example.rest.event.EventCategory;
import org.example.rest.event.EventNotFoundException;
import org.example.rest.event.EventRepository;
import org.example.rest.event.EventStatus;
import org.example.rest.hotel.Hotel;
import org.example.rest.hotel.HotelRepository;
import org.example.rest.security.SecurityUtil;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventReservationServiceImplTest {

    @Mock
    private EventReservationRepository repository;

    @Mock
    private EventReservationMapper mapper;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private HotelRepository hotelRepository;

    @InjectMocks
    private EventReservationServiceImpl service;

    private Hotel hotel;
    private Event event;
    private EventReservation reservation;
    private EventReservationRequestDto requestDto;
    private EventReservationResponseDto responseDto;

    @BeforeEach
    void setUp() {
        hotel = Hotel.builder()
                .id(1L)
                .name("Test Hotel")
                .managerId(100L)
                .build();

        event = new Event();
        event.setId(1L);
        event.setHotel(hotel);
        event.setTitle("Test Event");
        event.setStatus(EventStatus.PUBLISHED);
        event.setCapacityTotal(100);
        event.setMaxPerUser(5);
        event.setRequiresApproval(false);
        event.setStartDateTime(LocalDateTime.now().plusDays(5));
        event.setEndDateTime(LocalDateTime.now().plusDays(6));

        reservation = new EventReservation();
        reservation.setId(1L);
        reservation.setEvent(event);
        reservation.setUserId(200L);
        reservation.setReservationCode("RES001");
        reservation.setParticipantsCount(2);
        reservation.setTotalAmount(BigDecimal.valueOf(100.00));
        reservation.setCurrency("USD");
        reservation.setStatus(EventReservationStatus.CONFIRMED);

        requestDto = new EventReservationRequestDto();
        requestDto.setParticipantsCount(2);
        requestDto.setAgeRestriction(AgeRestriction.ALL_AGES);

        responseDto = new EventReservationResponseDto();
        responseDto.setId(1L);
        responseDto.setEventId(1L);
        responseDto.setReservationCode("RES001");
        responseDto.setParticipantsCount(2);
        responseDto.setStatus(EventReservationStatus.CONFIRMED);
    }

    @Test
    void testCreate_Success() {
        Long eventId = 1L;
        Long userId = 200L;

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(repository.countTotalParticipantsByEventId(eventId)).thenReturn(0);
        when(mapper.toEntity(requestDto, event)).thenReturn(reservation);
        when(repository.save(any(EventReservation.class))).thenReturn(reservation);
        when(mapper.toResponseDto(reservation)).thenReturn(responseDto);

        EventReservationResponseDto result = service.create(eventId, userId, requestDto);

        assertNotNull(result);
        assertEquals(responseDto.getId(), result.getId());
        verify(repository, times(1)).save(any(EventReservation.class));
    }

    @Test
    void testCreate_EventNotFound() {
        Long eventId = 999L;
        Long userId = 200L;

        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(EventNotFoundException.class,
                () -> service.create(eventId, userId, requestDto));

        verify(repository, never()).save(any());
    }

    @Test
    void testCreate_EventNotPublished() {
        Long eventId = 1L;
        Long userId = 200L;

        event.setStatus(EventStatus.DRAFT);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        assertThrows(EventReservationForbiddenActionException.class,
                () -> service.create(eventId, userId, requestDto));

        verify(repository, never()).save(any());
    }

    @Test
    void testCreate_ExceedsMaxPerUser() {
        Long eventId = 1L;
        Long userId = 200L;

        event.setMaxPerUser(1);
        requestDto.setParticipantsCount(2);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        assertThrows(EventReservationForbiddenActionException.class,
                () -> service.create(eventId, userId, requestDto));

        verify(repository, never()).save(any());
    }

    @Test
    void testCreate_ExceedsCapacity() {
        Long eventId = 1L;
        Long userId = 200L;

        event.setCapacityTotal(100);
        requestDto.setParticipantsCount(50);

        lenient().when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        lenient().when(repository.countTotalParticipantsByEventId(eventId)).thenReturn(60);

        assertThrows(EventReservationForbiddenActionException.class,
                () -> service.create(eventId, userId, requestDto));

        verify(repository, never()).save(any());
    }

    @Test
    void testCreate_RequiresApproval() {
        Long eventId = 1L;
        Long userId = 200L;

        event.setRequiresApproval(true);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(repository.countTotalParticipantsByEventId(eventId)).thenReturn(0);
        when(mapper.toEntity(requestDto, event)).thenReturn(reservation);
        when(repository.save(any(EventReservation.class))).thenReturn(reservation);
        when(mapper.toResponseDto(reservation)).thenReturn(responseDto);

        EventReservationResponseDto result = service.create(eventId, userId, requestDto);

        assertNotNull(result);
        verify(repository, times(1)).save(any(EventReservation.class));
    }

    @Test
    void testGetById_Success() {
        Long eventId = 1L;
        Long reservationId = 1L;
        Long userId = 200L;

        when(eventRepository.existsById(eventId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(mapper.toResponseDto(reservation)).thenReturn(responseDto);

        EventReservationResponseDto result = service.getById(eventId, reservationId, userId, "ADMIN");

        assertNotNull(result);
        verify(repository, times(1)).findById(reservationId);
    }

    @Test
    void testGetById_EventNotFound() {
        Long eventId = 999L;
        Long reservationId = 1L;
        Long userId = 200L;

        when(eventRepository.existsById(eventId)).thenReturn(false);

        assertThrows(EventNotFoundException.class,
                () -> service.getById(eventId, reservationId, userId, "ADMIN"));
    }

    @Test
    void testGetById_ReservationNotFound() {
        Long eventId = 1L;
        Long reservationId = 999L;
        Long userId = 200L;

        when(eventRepository.existsById(eventId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.empty());

        assertThrows(EventReservationNotFoundException.class,
                () -> service.getById(eventId, reservationId, userId, "ADMIN"));
    }

    @Test
    void testGetById_UserAccessDenied() {
        Long eventId = 1L;
        Long reservationId = 1L;
        Long userId = 999L; // Different user

        when(eventRepository.existsById(eventId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.of(reservation));

        assertThrows(AccessDeniedException.class,
                () -> service.getById(eventId, reservationId, userId, "USER"));

        verify(mapper, never()).toResponseDto(any());
    }

    @Test
    void testGetById_UserOwnReservation() {
        Long eventId = 1L;
        Long reservationId = 1L;
        Long userId = 200L; // Same user

        when(eventRepository.existsById(eventId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(mapper.toResponseDto(reservation)).thenReturn(responseDto);

        EventReservationResponseDto result = service.getById(eventId, reservationId, userId, "USER");

        assertNotNull(result);
        verify(mapper, times(1)).toResponseDto(any());
    }

    @Test
    void testList_Success() {
        Long eventId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<EventReservation> page = new PageImpl<>(List.of(reservation), pageable, 1);

        when(eventRepository.existsById(eventId)).thenReturn(true);
        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(mapper.toResponseDto(reservation)).thenReturn(responseDto);

        PagedResponse<EventReservationResponseDto> result = service.list(
                eventId, null, null, null, null, null, null, null, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testList_WithFilters() {
        Long eventId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<EventReservation> page = new PageImpl<>(List.of(reservation), pageable, 1);

        when(eventRepository.existsById(eventId)).thenReturn(true);
        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(mapper.toResponseDto(reservation)).thenReturn(responseDto);

        PagedResponse<EventReservationResponseDto> result = service.list(
                eventId,
                200L,
                EventReservationStatus.CONFIRMED,
                EventCategory.CONFERENCE,
                DifficultyLevel.MEDIUM,
                AgeRestriction.ADULTS_ONLY,
                1, 10,
                BigDecimal.ZERO, BigDecimal.valueOf(1000),
                pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testList_EmptyResult() {
        Long eventId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<EventReservation> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(eventRepository.existsById(eventId)).thenReturn(true);
        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

        PagedResponse<EventReservationResponseDto> result = service.list(
                eventId, null, null, null, null, null, null, null, null, null, pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void testUpdate_Success() {
        Long eventId = 1L;
        Long reservationId = 1L;
        Long managerId = 100L;

        event.setPrice(150L);

        when(eventRepository.existsById(eventId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(repository.save(any(EventReservation.class))).thenReturn(reservation);
        when(mapper.toResponseDto(reservation)).thenReturn(responseDto);

        EventReservationResponseDto result = service.update(eventId, reservationId, managerId, requestDto);

        assertNotNull(result);
        verify(repository, times(1)).save(any(EventReservation.class));
    }

    @Test
    void testUpdate_NotFound() {
        Long eventId = 1L;
        Long reservationId = 999L;
        Long managerId = 100L;

        when(eventRepository.existsById(eventId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.empty());

        assertThrows(EventReservationNotFoundException.class,
                () -> service.update(eventId, reservationId, managerId, requestDto));

        verify(repository, never()).save(any());
    }

    @Test
    void testConfirm_Success() {
        Long eventId = 1L;
        Long reservationId = 1L;
        Long managerId = 100L;

        reservation.setStatus(EventReservationStatus.PENDING);

        when(eventRepository.existsById(eventId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(repository.save(any(EventReservation.class))).thenReturn(reservation);
        when(mapper.toResponseDto(reservation)).thenReturn(responseDto);

        EventReservationResponseDto result = service.confirm(eventId, reservationId, managerId);

        assertNotNull(result);
        verify(repository, times(1)).save(any(EventReservation.class));
    }

    @Test
    void testCancel_Success() {
        Long eventId = 1L;
        Long reservationId = 1L;
        Long userId = 200L;

        when(eventRepository.existsById(eventId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(repository.save(any(EventReservation.class))).thenReturn(reservation);
        when(mapper.toResponseDto(reservation)).thenReturn(responseDto);

        EventReservationResponseDto result = service.cancel(eventId, reservationId, userId, "User request");

        assertNotNull(result);
        verify(repository, times(1)).save(any(EventReservation.class));
    }

    @Test
    void testCancel_NotFound() {
        Long eventId = 1L;
        Long reservationId = 999L;
        Long userId = 200L;

        when(eventRepository.existsById(eventId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.empty());

        assertThrows(EventReservationNotFoundException.class,
                () -> service.cancel(eventId, reservationId, userId, "User request"));

        verify(repository, never()).save(any());
    }

    @Test
    void testComplete_Success() {
        Long eventId = 1L;
        Long reservationId = 1L;
        Long managerId = 100L;

        when(eventRepository.existsById(eventId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(repository.save(any(EventReservation.class))).thenReturn(reservation);
        when(mapper.toResponseDto(reservation)).thenReturn(responseDto);

        EventReservationResponseDto result = service.complete(eventId, reservationId, managerId);

        assertNotNull(result);
        verify(repository, times(1)).save(any(EventReservation.class));
    }

    @Test
    void testAttend_Success() {
        Long eventId = 1L;
        Long reservationId = 1L;
        Long managerId = 100L;

        when(eventRepository.existsById(eventId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(repository.save(any(EventReservation.class))).thenReturn(reservation);
        when(mapper.toResponseDto(reservation)).thenReturn(responseDto);

        EventReservationResponseDto result = service.attend(eventId, reservationId, managerId);

        assertNotNull(result);
        verify(repository, times(1)).save(any(EventReservation.class));
    }

    @Test
    void testNoShow_Success() {
        Long eventId = 1L;
        Long reservationId = 1L;
        Long managerId = 100L;

        when(eventRepository.existsById(eventId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(repository.save(any(EventReservation.class))).thenReturn(reservation);
        when(mapper.toResponseDto(reservation)).thenReturn(responseDto);

        EventReservationResponseDto result = service.noShow(eventId, reservationId, managerId);

        assertNotNull(result);
        verify(repository, times(1)).save(any(EventReservation.class));
    }

    @Test
    void testDelete_Success() {
        Long eventId = 1L;
        Long reservationId = 1L;

        when(eventRepository.existsById(eventId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.of(reservation));

        service.delete(eventId, reservationId);

        verify(repository, times(1)).delete(any(EventReservation.class));
    }

    @Test
    void testDelete_NotFound() {
        Long eventId = 1L;
        Long reservationId = 999L;

        when(eventRepository.existsById(eventId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.empty());

        assertThrows(EventReservationNotFoundException.class,
                () -> service.delete(eventId, reservationId));

        verify(repository, never()).delete(any(EventReservation.class));
    }
}
