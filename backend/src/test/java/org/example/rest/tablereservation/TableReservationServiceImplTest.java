package org.example.rest.tablereservation;

import jakarta.mail.MessagingException;
import org.example.rest.PagedResponse;
import org.example.rest.hotel.Hotel;
import org.example.rest.hotel.HotelNotFoundException;
import org.example.rest.hotel.HotelRepository;
import org.example.rest.notification.EmailService;
import org.example.rest.security.SecurityUtil;
import org.example.rest.security.user.AppUser;
import org.example.rest.security.user.UserRepository;
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
class TableReservationServiceImplTest {

    @Mock
    private TableReservationRepository repository;

    @Mock
    private TableReservationMapper mapper;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private TableReservationServiceImpl service;

    private Hotel hotel;
    private AppUser user;
    private TableReservation reservation;
    private TableReservationRequestDto requestDto;
    private TableReservationResponseDto responseDto;

    @BeforeEach
    void setUp() {
        hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Test Hotel");
        hotel.setManagerId(100L);

        user = new AppUser();
        user.setId(200L);
        user.setEmail("user@example.com");
        user.setFirstName("John");

        reservation = new TableReservation();
        reservation.setId(1L);
        reservation.setHotel(hotel);
        reservation.setUserId(200L);
        reservation.setReservationCode("RES001");
        reservation.setGuestCount(4);
        reservation.setReservationDateTime(LocalDateTime.now().plusHours(2));
        reservation.setDurationMinutes(90);
        reservation.setTableNumber("T1");
        reservation.setTableType(TableType.STANDARD);
        reservation.setStatus(TableReservationStatus.PENDING);

        requestDto = new TableReservationRequestDto();
        requestDto.setGuestCount(4);
        requestDto.setReservationDateTime(LocalDateTime.now().plusHours(2));
        requestDto.setDurationMinutes(90);
        requestDto.setTableNumber("T1");
        requestDto.setTableType(TableType.STANDARD);

        responseDto = new TableReservationResponseDto();
        responseDto.setId(1L);
        responseDto.setReservationCode("RES001");
        responseDto.setGuestCount(4);
        responseDto.setStatus(TableReservationStatus.PENDING);
    }

    @Test
    void testCreate_Success() throws jakarta.mail.MessagingException {
        Long hotelId = 1L;
        Long userId = 200L;

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(repository.existsByHotelIdAndReservationDateTimeAndTableNumberAndStatusNot(
                hotelId, requestDto.getReservationDateTime(), requestDto.getTableNumber(),
                TableReservationStatus.CANCELLED)).thenReturn(false);
        when(mapper.toEntity(requestDto, hotel)).thenReturn(reservation);
        when(repository.save(any(TableReservation.class))).thenReturn(reservation);
        when(mapper.toResponseDto(reservation)).thenReturn(responseDto);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        TableReservationResponseDto result = service.create(hotelId, userId, requestDto);

        assertNotNull(result);
        verify(repository, times(1)).save(any(TableReservation.class));
        verify(emailService, times(1)).sendTableReservationConfirmation(
                anyString(), anyString(), any(TableReservation.class));
    }

    @Test
    void testCreate_HotelNotFound() {
        Long hotelId = 999L;
        Long userId = 200L;

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

        assertThrows(HotelNotFoundException.class,
                () -> service.create(hotelId, userId, requestDto));

        verify(repository, never()).save(any());
    }

    @Test
    void testCreate_TableConflict() {
        Long hotelId = 1L;
        Long userId = 200L;

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(repository.existsByHotelIdAndReservationDateTimeAndTableNumberAndStatusNot(
                hotelId, requestDto.getReservationDateTime(), requestDto.getTableNumber(),
                TableReservationStatus.CANCELLED)).thenReturn(true);

        assertThrows(TableReservationForbiddenActionException.class,
                () -> service.create(hotelId, userId, requestDto));

        verify(repository, never()).save(any());
    }

    @Test
    void testGetById_Success_User() {
        Long hotelId = 1L;
        Long reservationId = 1L;
        Long userId = 200L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(mapper.toResponseDto(reservation)).thenReturn(responseDto);

        TableReservationResponseDto result = service.getById(hotelId, reservationId, userId, "USER");

        assertNotNull(result);
        verify(repository, times(1)).findById(reservationId);
    }

    @Test
    void testGetById_UserAccessDenied() {
        Long hotelId = 1L;
        Long reservationId = 1L;
        Long differentUserId = 999L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.of(reservation));

        assertThrows(AccessDeniedException.class,
                () -> service.getById(hotelId, reservationId, differentUserId, "USER"));
    }

    @Test
    void testGetById_HotelNotFound() {
        Long hotelId = 999L;
        Long reservationId = 1L;

        when(hotelRepository.existsById(hotelId)).thenReturn(false);

        assertThrows(HotelNotFoundException.class,
                () -> service.getById(hotelId, reservationId, 200L, "USER"));
    }

    @Test
    void testGetById_ReservationNotFound() {
        Long hotelId = 1L;
        Long reservationId = 999L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.empty());

        assertThrows(TableReservationNotFoundException.class,
                () -> service.getById(hotelId, reservationId, 200L, "USER"));
    }

    @Test
    void testList_Success() {
        Long hotelId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<TableReservation> page = new PageImpl<>(List.of(reservation), pageable, 1);

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(mapper.toResponseDto(reservation)).thenReturn(responseDto);

        PagedResponse<TableReservationResponseDto> result = service.list(
                hotelId, null, null, null, null, null, null, null, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testList_InvalidGuestRange() {
        Long hotelId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        when(hotelRepository.existsById(hotelId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.list(hotelId, null, null, null, null, 10, 5, null, null, null, pageable));
    }

    @Test
    void testList_InvalidDateRange() {
        Long hotelId = 1L;
        LocalDateTime from = LocalDateTime.now().plusHours(2);
        LocalDateTime to = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10);

        when(hotelRepository.existsById(hotelId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.list(hotelId, null, null, null, null, null, null, from, to, null, pageable));
    }

    @Test
    void testUpdate_ByUser_Success() {
        Long hotelId = 1L;
        Long reservationId = 1L;
        Long userId = 200L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(repository.existsByHotelIdAndReservationDateTimeAndTableNumberAndStatusNotAndIdNot(
                hotelId, requestDto.getReservationDateTime(), requestDto.getTableNumber(),
                TableReservationStatus.CANCELLED, reservationId)).thenReturn(false);
        when(repository.save(any(TableReservation.class))).thenReturn(reservation);
        when(mapper.toResponseDto(reservation)).thenReturn(responseDto);

        TableReservationResponseDto result = service.update(hotelId, reservationId, userId, "USER", requestDto);

        assertNotNull(result);
        verify(repository, times(1)).save(any(TableReservation.class));
    }

    @Test
    void testUpdate_UserNotOwner() {
        Long hotelId = 1L;
        Long reservationId = 1L;
        Long differentUserId = 999L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.of(reservation));

        assertThrows(AccessDeniedException.class,
                () -> service.update(hotelId, reservationId, differentUserId, "USER", requestDto));

        verify(repository, never()).save(any());
    }

    @Test
    void testUpdate_CancelledReservation() {
        Long hotelId = 1L;
        Long reservationId = 1L;
        Long userId = 200L;

        reservation.setStatus(TableReservationStatus.CANCELLED);

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.of(reservation));

        assertThrows(TableReservationForbiddenActionException.class,
                () -> service.update(hotelId, reservationId, userId, "USER", requestDto));

        verify(repository, never()).save(any());
    }

    @Test
    void testConfirm_Success() {
        Long hotelId = 1L;
        Long reservationId = 1L;
        Long managerId = 100L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(repository.save(any(TableReservation.class))).thenReturn(reservation);
        when(mapper.toResponseDto(reservation)).thenReturn(responseDto);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            TableReservationResponseDto result = service.confirm(hotelId, reservationId, managerId);

            assertNotNull(result);
            assertEquals(TableReservationStatus.CONFIRMED, reservation.getStatus());
            verify(repository, times(1)).save(any(TableReservation.class));
        }
    }

    @Test
    void testConfirm_NotPending() {
        Long hotelId = 1L;
        Long reservationId = 1L;
        Long managerId = 100L;

        reservation.setStatus(TableReservationStatus.CONFIRMED);

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.of(reservation));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            assertThrows(TableReservationForbiddenActionException.class,
                    () -> service.confirm(hotelId, reservationId, managerId));

            verify(repository, never()).save(any());
        }
    }

    @Test
    void testCancel_ByUser_Success() {
        Long hotelId = 1L;
        Long reservationId = 1L;
        Long userId = 200L;

        reservation.setStatus(TableReservationStatus.PENDING);
        reservation.setUserId(userId);
        reservation.setReservationDateTime(LocalDateTime.now().plusHours(2));

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(repository.save(any(TableReservation.class))).thenAnswer(invocation -> {
            TableReservation arg = invocation.getArgument(0);
            arg.setStatus(TableReservationStatus.CANCELLED);
            arg.setCancelledAt(LocalDateTime.now());
            return arg;
        });
        when(mapper.toResponseDto(any(TableReservation.class))).thenReturn(responseDto);

        // no emails sent for user cancellation
        TableReservationResponseDto result = service.cancel(hotelId, reservationId, userId, "USER");

        assertNotNull(result);
        verify(repository, times(1)).save(any(TableReservation.class));
        try {
            verify(emailService, never()).sendTableCancelledByManager(anyString(), anyString(),
                    any(TableReservation.class), anyList());
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testCancel_ByHotelManager_Success() throws MessagingException {
        Long hotelId = 1L;
        Long reservationId = 1L;
        Long managerId = 100L;
        Long userId = 200L;

        reservation.setStatus(TableReservationStatus.PENDING);
        reservation.setUserId(userId);
        reservation.setReservationDateTime(LocalDateTime.now().plusHours(2));
        reservation.setReservationCode("RES001");

        AppUser reservationUser = new AppUser();
        reservationUser.setId(userId);
        reservationUser.setFirstName("Guest");
        reservationUser.setEmail("guest@example.com");

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(repository.save(any(TableReservation.class))).thenAnswer(invocation -> {
            TableReservation arg = invocation.getArgument(0);
            arg.setStatus(TableReservationStatus.CANCELLED);
            arg.setCancelledAt(LocalDateTime.now());
            return arg;
        });
        when(mapper.toResponseDto(any(TableReservation.class))).thenReturn(responseDto);
        when(repository.findByHotelIdAndStatus(hotelId, TableReservationStatus.PENDING))
                .thenReturn(List.of());
        when(userRepository.findById(userId)).thenReturn(Optional.of(reservationUser));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            TableReservationResponseDto result = service.cancel(hotelId, reservationId, managerId, "HOTEL_MANAGER");

            assertNotNull(result);
            verify(repository, times(1)).save(any(TableReservation.class));
            // emails are sent for manager cancellation
            verify(emailService, times(1)).sendTableCancelledByManager(anyString(), anyString(),
                    any(TableReservation.class), anyList());
            verify(emailService, times(1)).sendManagerCancellationRefund(anyString(), anyString(),
                    anyString(), isNull());
        }
    }

    @Test
    void testCancel_UserTooClose_ThrowsException() {
        Long hotelId = 1L;
        Long reservationId = 1L;
        Long userId = 200L;

        reservation.setStatus(TableReservationStatus.PENDING);
        reservation.setUserId(userId);
        reservation.setReservationDateTime(LocalDateTime.now().plusMinutes(30)); // Only 30 mins

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.of(reservation));

        assertThrows(TableReservationForbiddenActionException.class,
                () -> service.cancel(hotelId, reservationId, userId, "USER"));

        verify(repository, never()).save(any());
    }

    @Test
    void testCancel_UserNotOwner_ThrowsException() {
        Long hotelId = 1L;
        Long reservationId = 1L;
        Long userId = 200L;
        Long differentUserId = 999L;

        reservation.setStatus(TableReservationStatus.PENDING);
        reservation.setUserId(userId);
        reservation.setReservationDateTime(LocalDateTime.now().plusHours(2));

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.of(reservation));

        assertThrows(AccessDeniedException.class,
                () -> service.cancel(hotelId, reservationId, differentUserId, "USER"));

        verify(repository, never()).save(any());
    }

    @Test
    void testCancel_UserAccessDenied_ThrowsException() {
        Long hotelId = 1L;
        Long reservationId = 1L;
        Long userId = 200L;
        Long differentUserId = 999L;

        reservation.setStatus(TableReservationStatus.PENDING);
        reservation.setUserId(userId);

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.of(reservation));

        assertThrows(AccessDeniedException.class,
                () -> service.cancel(hotelId, reservationId, differentUserId, "USER"));

        verify(repository, never()).save(any());
    }

    @Test
    void testCancel_AlreadyCancelled_ThrowsException() {
        Long hotelId = 1L;
        Long reservationId = 1L;
        Long userId = 200L;

        reservation.setStatus(TableReservationStatus.CANCELLED);
        reservation.setUserId(userId);

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.of(reservation));

        assertThrows(TableReservationForbiddenActionException.class,
                () -> service.cancel(hotelId, reservationId, userId, "USER"));

        verify(repository, never()).save(any());
    }

    @Test
    void testCancel_Completed_ThrowsException() {
        Long hotelId = 1L;
        Long reservationId = 1L;
        Long userId = 200L;

        reservation.setStatus(TableReservationStatus.COMPLETED);
        reservation.setUserId(userId);

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.of(reservation));

        assertThrows(TableReservationForbiddenActionException.class,
                () -> service.cancel(hotelId, reservationId, userId, "USER"));

        verify(repository, never()).save(any());
    }

    @Test
    void testCancel_WithinOneHour() {
        Long hotelId = 1L;
        Long reservationId = 1L;
        Long userId = 200L;

        reservation.setReservationDateTime(LocalDateTime.now().plusMinutes(30)); // 30 minutes away

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.of(reservation));

        assertThrows(TableReservationForbiddenActionException.class,
                () -> service.cancel(hotelId, reservationId, userId, "USER"));

        verify(repository, never()).save(any());
    }

    @Test
    void testCancel_AlreadyCancelled() {
        Long hotelId = 1L;
        Long reservationId = 1L;
        Long userId = 200L;

        reservation.setStatus(TableReservationStatus.CANCELLED);

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.of(reservation));

        assertThrows(TableReservationForbiddenActionException.class,
                () -> service.cancel(hotelId, reservationId, userId, "USER"));

        verify(repository, never()).save(any());
    }

    @Test
    void testCancel_ByManager() throws jakarta.mail.MessagingException {
        Long hotelId = 1L;
        Long reservationId = 1L;
        Long managerId = 100L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(repository.findByHotelIdAndStatus(hotelId, TableReservationStatus.PENDING)).thenReturn(List.of());
        when(repository.save(any(TableReservation.class))).thenAnswer(invocation -> {
            TableReservation saved = invocation.getArgument(0);
            return saved;
        });
        when(mapper.toResponseDto(any(TableReservation.class))).thenReturn(responseDto);
        when(userRepository.findById(200L)).thenReturn(Optional.of(user));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            TableReservationResponseDto result = service.cancel(hotelId, reservationId, managerId, "HOTEL_MANAGER");

            assertNotNull(result);
            verify(emailService, atLeastOnce()).sendTableCancelledByManager(
                    anyString(), anyString(), any(TableReservation.class), any(List.class));
        }
    }

    @Test
    void testComplete_Success() {
        Long hotelId = 1L;
        Long reservationId = 1L;
        Long managerId = 100L;

        reservation.setStatus(TableReservationStatus.CONFIRMED);

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(repository.save(any(TableReservation.class))).thenReturn(reservation);
        when(mapper.toResponseDto(reservation)).thenReturn(responseDto);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            TableReservationResponseDto result = service.complete(hotelId, reservationId, managerId);

            assertNotNull(result);
            assertEquals(TableReservationStatus.COMPLETED, reservation.getStatus());
            verify(repository, times(1)).save(any(TableReservation.class));
        }
    }

    @Test
    void testComplete_NotConfirmed() {
        Long hotelId = 1L;
        Long reservationId = 1L;
        Long managerId = 100L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.of(reservation));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            assertThrows(TableReservationForbiddenActionException.class,
                    () -> service.complete(hotelId, reservationId, managerId));

            verify(repository, never()).save(any());
        }
    }

    @Test
    void testNoShow_Success() {
        Long hotelId = 1L;
        Long reservationId = 1L;
        Long managerId = 100L;

        reservation.setStatus(TableReservationStatus.CONFIRMED);

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(repository.save(any(TableReservation.class))).thenReturn(reservation);
        when(mapper.toResponseDto(reservation)).thenReturn(responseDto);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            TableReservationResponseDto result = service.noShow(hotelId, reservationId, managerId);

            assertNotNull(result);
            assertEquals(TableReservationStatus.NO_SHOW, reservation.getStatus());
            verify(repository, times(1)).save(any(TableReservation.class));
        }
    }

    @Test
    void testDelete_Success() {
        Long hotelId = 1L;
        Long reservationId = 1L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.of(reservation));

        service.delete(hotelId, reservationId);

        verify(repository, times(1)).delete(any(TableReservation.class));
    }

    @Test
    void testDelete_NotFound() {
        Long hotelId = 1L;
        Long reservationId = 999L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(reservationId)).thenReturn(Optional.empty());

        assertThrows(TableReservationNotFoundException.class,
                () -> service.delete(hotelId, reservationId));

        verify(repository, never()).delete(any(TableReservation.class));
    }
}
