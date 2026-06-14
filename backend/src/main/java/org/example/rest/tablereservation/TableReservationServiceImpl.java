package org.example.rest.tablereservation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rest.PagedResponse;
import org.example.rest.hotel.Hotel;
import org.example.rest.hotel.HotelNotFoundException;
import org.example.rest.hotel.HotelRepository;
import org.example.rest.security.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.example.rest.notification.EmailService;
import org.example.rest.security.user.UserRepository;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TableReservationServiceImpl implements TableReservationService {

    private final TableReservationRepository repository;
    private final TableReservationMapper mapper;
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public TableReservationResponseDto create(Long hotelId, Long userId, TableReservationRequestDto dto) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException(hotelId));

        if (dto.getTableNumber() != null) {
            boolean conflict = repository.existsByHotelIdAndReservationDateTimeAndTableNumberAndStatusNot(
                    hotelId, dto.getReservationDateTime(), dto.getTableNumber(), TableReservationStatus.CANCELLED);
            if (conflict)
                throw new TableReservationForbiddenActionException(
                        "Table " + dto.getTableNumber() + " is already reserved at " + dto.getReservationDateTime());
        }

        TableReservation reservation = mapper.toEntity(dto, hotel);
        reservation.setUserId(userId);
        TableReservation saved = repository.save(reservation);
        log.info("Created table reservation id={} for hotelId={} by userId={}", saved.getId(), hotelId, userId);

        // send confirmation email to user
        userRepository.findById(userId).ifPresent(user -> {
            try {
                emailService.sendTableReservationConfirmation(user.getEmail(), user.getFirstName(), saved);
            } catch (Exception e) {
                log.warn("Table reservation confirmation email failed for userId={} — {}", userId, e.getMessage());
            }
        });

        return mapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TableReservationResponseDto getById(Long hotelId, Long id, Long currentUserId, String role) {
        if (!hotelRepository.existsById(hotelId))
            throw new HotelNotFoundException(hotelId);

        TableReservation reservation = repository.findById(id)
                .orElseThrow(() -> new TableReservationNotFoundException(id));

        if (!reservation.getHotel().getId().equals(hotelId))
            throw new TableReservationNotFoundException(id);

        if ("USER".equals(role) && !reservation.getUserId().equals(currentUserId))
            throw new AccessDeniedException("You can only view your own reservations");

        return mapper.toResponseDto(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TableReservationResponseDto> list(
            Long hotelId, Long userId,
            TableReservationStatus status, TableType tableType,
            SpecialOccasion specialOccasion, Integer minGuests, Integer maxGuests,
            LocalDateTime from, LocalDateTime to, String tableNumber, Pageable pageable) {

        if (!hotelRepository.existsById(hotelId))
            throw new HotelNotFoundException(hotelId);

        if (minGuests != null && maxGuests != null && minGuests > maxGuests)
            throw new IllegalArgumentException("minGuests must not be greater than maxGuests");

        if (from != null && to != null && from.isAfter(to))
            throw new IllegalArgumentException("'from' date must not be after 'to' date");

        Specification<TableReservation> spec = Specification
                .where(TableReservationSpecification.hasHotelId(hotelId))
                .and(TableReservationSpecification.hasUserId(userId))
                .and(TableReservationSpecification.hasStatus(status))
                .and(TableReservationSpecification.hasTableType(tableType))
                .and(TableReservationSpecification.hasSpecialOccasion(specialOccasion))
                .and(TableReservationSpecification.hasGuestCountGreaterThanOrEqual(minGuests))
                .and(TableReservationSpecification.hasGuestCountLessThanOrEqual(maxGuests))
                .and(TableReservationSpecification.reservationAfter(from))
                .and(TableReservationSpecification.reservationBefore(to))
                .and(TableReservationSpecification.hasTableNumber(tableNumber));

        Page<TableReservation> page = repository.findAll(spec, pageable);

        return new PagedResponse<>(
                page.map(mapper::toResponseDto).getContent(),
                page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isLast()
        );
    }

    @Override
    @Transactional
    public TableReservationResponseDto update(Long hotelId, Long id, Long currentUserId, String role,
                                              TableReservationRequestDto dto) {
        TableReservation reservation = findAndValidate(hotelId, id);

        if ("USER".equals(role) && !reservation.getUserId().equals(currentUserId))
            throw new AccessDeniedException("You can only update your own reservations");

        if ("HOTEL_MANAGER".equals(role))
            verifyManagerOwnership(reservation.getHotel(), currentUserId);

        if (reservation.getStatus() == TableReservationStatus.CANCELLED
                || reservation.getStatus() == TableReservationStatus.COMPLETED)
            throw new TableReservationForbiddenActionException(
                    "Cannot update a reservation with status: " + reservation.getStatus().getDisplayName());

        if (dto.getTableNumber() != null) {
            boolean conflict = repository.existsByHotelIdAndReservationDateTimeAndTableNumberAndStatusNotAndIdNot(
                    hotelId, dto.getReservationDateTime(), dto.getTableNumber(), TableReservationStatus.CANCELLED, id);
            if (conflict)
                throw new TableReservationForbiddenActionException(
                        "Table " + dto.getTableNumber() + " is already reserved at " + dto.getReservationDateTime());
        }

        reservation.setGuestCount(dto.getGuestCount());
        reservation.setReservationDateTime(dto.getReservationDateTime());
        reservation.setDurationMinutes(dto.getDurationMinutes() != null ? dto.getDurationMinutes() : reservation.getDurationMinutes());
        reservation.setSpecialOccasion(dto.getSpecialOccasion());
        reservation.setTableNumber(dto.getTableNumber());
        reservation.setTableType(dto.getTableType());
        reservation.setPreOrderItems(dto.getPreOrderItems());
        reservation.setDietaryRestrictions(dto.getDietaryRestrictions());

        log.info("Updated table reservation id={} by userId={}", id, currentUserId);
        return mapper.toResponseDto(repository.save(reservation));
    }

    @Override
    @Transactional
    public TableReservationResponseDto confirm(Long hotelId, Long id, Long managerId) {
        TableReservation reservation = findAndValidate(hotelId, id);
        verifyManagerOwnership(reservation.getHotel(), managerId);
        if (reservation.getStatus() != TableReservationStatus.PENDING)
            throw new TableReservationForbiddenActionException("Only pending reservations can be confirmed");
        reservation.setStatus(TableReservationStatus.CONFIRMED);
        log.info("Confirmed table reservation id={} by managerId={}", id, managerId);
        return mapper.toResponseDto(repository.save(reservation));
    }

    @Override
    @Transactional
    public TableReservationResponseDto cancel(Long hotelId, Long id, Long currentUserId, String role) {
        TableReservation reservation = findAndValidate(hotelId, id);

        if ("USER".equals(role)) {
            if (!reservation.getUserId().equals(currentUserId))
                throw new AccessDeniedException("You can only cancel your own reservations");
            if (reservation.getReservationDateTime() != null) {
                long minutesUntilReservation = ChronoUnit.MINUTES.between(
                        LocalDateTime.now(), reservation.getReservationDateTime());
                if (minutesUntilReservation < 60) {
                    throw new TableReservationForbiddenActionException(
                            "Cancellation is not allowed within 1 hour of the reservation time. " +
                                    "Your reservation is in " + minutesUntilReservation + " minute(s).");
                }
            }
        } else if ("HOTEL_MANAGER".equals(role)) {
            verifyManagerOwnership(reservation.getHotel(), currentUserId);
        }

        if (reservation.getStatus() == TableReservationStatus.COMPLETED)
            throw new TableReservationForbiddenActionException("Completed reservations cannot be cancelled");
        if (reservation.getStatus() == TableReservationStatus.CANCELLED)
            throw new TableReservationForbiddenActionException("Reservation is already cancelled");

        reservation.setStatus(TableReservationStatus.CANCELLED);
        reservation.setCancelledAt(LocalDateTime.now());
        TableReservationResponseDto result = mapper.toResponseDto(repository.save(reservation));
        log.info("Cancelled table reservation id={} by userId={} role={}", id, currentUserId, role);

        if ("HOTEL_MANAGER".equals(role) || "ADMIN".equals(role)) {
            if (reservation.getUserId() != null) {
                List<TableReservation> otherTables = getOtherAvailableTables(hotelId, reservation);
                userRepository.findById(reservation.getUserId()).ifPresent(user -> {
                    try {
                        emailService.sendTableCancelledByManager(
                                user.getEmail(), user.getFirstName(), reservation, otherTables);
                    } catch (Exception e) {
                        log.warn("Table cancellation email failed for userId={} — {}",
                                reservation.getUserId(), e.getMessage());
                    }
                    try {
                        emailService.sendManagerCancellationRefund(
                                user.getEmail(), user.getFirstName(),
                                "Table reservation " + reservation.getReservationCode()
                                        + " at " + (reservation.getHotel() != null ? reservation.getHotel().getName() : ""),
                                null);
                    } catch (Exception e) {
                        log.warn("Table refund email failed for userId={} — {}",
                                reservation.getUserId(), e.getMessage());
                    }
                });
            }
        }

        return result;
    }

    private List<TableReservation> getOtherAvailableTables(Long hotelId, TableReservation cancelled) {
        return repository
                .findByHotelIdAndStatus(hotelId, TableReservationStatus.PENDING)
                .stream()
                .filter(t -> !t.getId().equals(cancelled.getId()))
                .filter(t -> t.getReservationDateTime() != null
                        && t.getReservationDateTime().isAfter(LocalDateTime.now()))
                .limit(3)
                .toList();
    }

    @Override
    @Transactional
    public TableReservationResponseDto complete(Long hotelId, Long id, Long managerId) {
        TableReservation reservation = findAndValidate(hotelId, id);
        verifyManagerOwnership(reservation.getHotel(), managerId);
        if (reservation.getStatus() != TableReservationStatus.CONFIRMED)
            throw new TableReservationForbiddenActionException("Only confirmed reservations can be completed");
        reservation.setStatus(TableReservationStatus.COMPLETED);
        log.info("Completed table reservation id={} by managerId={}", id, managerId);
        return mapper.toResponseDto(repository.save(reservation));
    }

    @Override
    @Transactional
    public TableReservationResponseDto noShow(Long hotelId, Long id, Long managerId) {
        TableReservation reservation = findAndValidate(hotelId, id);
        verifyManagerOwnership(reservation.getHotel(), managerId);
        if (reservation.getStatus() != TableReservationStatus.CONFIRMED)
            throw new TableReservationForbiddenActionException("Only confirmed reservations can be marked as no-show");
        reservation.setStatus(TableReservationStatus.NO_SHOW);
        log.info("Marked table reservation id={} as no-show by managerId={}", id, managerId);
        return mapper.toResponseDto(repository.save(reservation));
    }

    @Override
    @Transactional
    public void delete(Long hotelId, Long id) {
        TableReservation reservation = findAndValidate(hotelId, id);
        repository.delete(reservation);
        log.info("Deleted table reservation id={}", id);
    }

    private TableReservation findAndValidate(Long hotelId, Long id) {
        if (!hotelRepository.existsById(hotelId))
            throw new HotelNotFoundException(hotelId);
        TableReservation reservation = repository.findById(id)
                .orElseThrow(() -> new TableReservationNotFoundException(id));
        if (!reservation.getHotel().getId().equals(hotelId))
            throw new TableReservationNotFoundException(id);
        return reservation;
    }

    private void verifyManagerOwnership(Hotel hotel, Long managerId) {
        String role = SecurityUtil.getCurrentUserRole();
        if ("ADMIN".equals(role)) return;
        if (!hotel.getManagerId().equals(managerId))
            throw new AccessDeniedException(
                    "You do not have permission to manage reservations for hotel id=" + hotel.getId());
    }
}