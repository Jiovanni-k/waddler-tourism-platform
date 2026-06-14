package org.example.rest.booking;

import lombok.RequiredArgsConstructor;
import org.example.rest.PagedResponse;
import org.example.rest.booking.event.BookingCancellationDeniedEvent;
import org.example.rest.booking.event.BookingCancelledEvent;
import org.example.rest.booking.event.BookingConfirmedEvent;
import org.example.rest.booking.event.BookingCreatedEvent;
import org.example.rest.cancellationpolicy.CancellationPolicyName;
import org.example.rest.cancellationpolicy.CancellationPolicyRepository;
import org.example.rest.inventory.Inventory;
import org.example.rest.inventory.InventoryRepository;
import org.example.rest.payment.Payment;
import org.example.rest.payment.PaymentRepository;
import org.example.rest.payment.refund.RefundRepository;
import org.example.rest.pricingrule.PricingRuleService;
import org.example.rest.room.Room;
import org.example.rest.room.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * Observer Pattern — BookingServiceImpl is the Subject (Publisher).
 *
 * It publishes domain events via ApplicationEventPublisher whenever a booking
 * changes state. All notification side-effects (emails, loyalty points) have
 * been moved out of this class entirely into BookingNotificationListener.
 *
 * BEFORE: This class had 7 try/catch email blocks and a loyaltyService call
 *         embedded inside create(), confirm(), and cancel().
 * AFTER:  This class knows nothing about emails or loyalty. It only publishes
 *         an event and lets observers react independently.
 */
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);

    private final BookingRepository repository;
    private final BookingMapper mapper;
    private final RoomRepository roomRepository;
    private final CancellationPolicyRepository cancellationPolicyRepository;
    private final PricingRuleService pricingRuleService;
    private final InventoryRepository inventoryRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;

    // Observer Pattern: the publisher — decouples this service from all observers
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public BookingResponseDto create(BookingRequestDto dto, Long userId) {
        if (Objects.isNull(userId)) {
            throw new IllegalArgumentException("Could not resolve authenticated user");
        }

        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Room not found with id: " + dto.getRoomId()));

        if (!Boolean.TRUE.equals(room.getActive())) {
            throw new BookingForbiddenActionException(
                    "Room id=" + room.getId() + " is not available for booking. It may be under maintenance or temporarily unavailable.");
        }

        validateBookingRequest(dto, room);

        BigDecimal calculatedPrice = pricingRuleService.calculateTotalPrice(
                room.getId(),
                dto.getCheckInDate(),
                dto.getCheckOutDate()
        );

        checkInventoryAvailable(room, dto.getCheckInDate(), dto.getCheckOutDate());
        reserveInventory(room, dto.getCheckInDate(), dto.getCheckOutDate());

        Booking booking = mapper.toEntity(dto, userId, room);
        booking.setTotalPrice(calculatedPrice.doubleValue());

        if (Objects.nonNull(room.getCancellationPolicyId())) {
            cancellationPolicyRepository.findById(room.getCancellationPolicyId())
                    .ifPresent(policy -> {
                        booking.setCancellationPolicyName(policy.getName());
                        booking.setCancellationPolicyDescription(policy.getDescription());
                        booking.setCancellationDaysBeforeCheckin(policy.getDaysBeforeCheckin());
                        booking.setCancellationRefundPercentage(policy.getRefundPercentage());
                    });
        }

        Booking saved = repository.save(booking);
        log.info("Created booking id={} for hotelId={} userId={}", saved.getId(), room.getHotel().getId(), userId);

        // Observer Pattern: publish event — BookingNotificationListener will send
        // the pending emails to the guest and hotel manager.
        String hotelName = room.getHotel().getName();
        Long managerId = room.getHotel() != null ? room.getHotel().getManagerId() : null;
        eventPublisher.publishEvent(new BookingCreatedEvent(saved, hotelName, managerId));

        return mapper.toResponseDto(saved);
    }

    @Override
    public BookingResponseDto getById(Long id, Long currentUserId, String role) {
        Booking booking = repository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));

        if ("ADMIN".equals(role)) {
            return mapper.toResponseDto(booking);
        }

        if ("USER".equals(role)) {
            if (!Objects.equals(booking.getUserId(), currentUserId)) {
                throw new BookingAccessDeniedException("You are not allowed to view this booking");
            }
            return mapper.toResponseDto(booking);
        }

        if ("HOTEL_MANAGER".equals(role)) {
            Long managerId = booking.getRoom() != null
                    && booking.getRoom().getHotel() != null
                    ? booking.getRoom().getHotel().getManagerId()
                    : null;

            if (!Objects.equals(managerId, currentUserId)) {
                throw new BookingAccessDeniedException("You are not allowed to view this booking");
            }
            return mapper.toResponseDto(booking);
        }

        throw new BookingAccessDeniedException("You are not allowed to view this booking");
    }

    @Override
    public PagedResponse<BookingResponseDto> list(
            Long userId,
            BookingStatus status,
            Long hotelId,
            Double minPrice,
            Double maxPrice,
            CancellationPolicyName policyName,
            Pageable pageable) {

        validatePriceRange(minPrice, maxPrice);

        Specification<Booking> spec = Specification
                .where(BookingSpecification.hasUserId(userId))
                .and(BookingSpecification.hasStatus(status))
                .and(BookingSpecification.hasHotelId(hotelId))
                .and(BookingSpecification.minPrice(minPrice))
                .and(BookingSpecification.maxPrice(maxPrice))
                .and(BookingSpecification.hasCancellationPolicyName(policyName));

        Page<Booking> page = repository.findAll(spec, pageable);

        return new PagedResponse<>(
                page.map(mapper::toResponseDto).getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @Override
    public BookingResponseDto confirm(Long id, Long currentUserId, String role) {
        Booking booking = repository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));

        validateBookingAccess(booking, currentUserId, role);

        if (Objects.equals(booking.getStatus(), BookingStatus.CONFIRMED)) {
            throw new BookingForbiddenActionException("This booking has already been confirmed");
        }
        if (Objects.equals(booking.getStatus(), BookingStatus.CANCELLED)) {
            throw new BookingForbiddenActionException("Cannot confirm a cancelled booking");
        }
        if (Objects.equals(booking.getStatus(), BookingStatus.COMPLETED)) {
            throw new BookingForbiddenActionException("Cannot confirm a completed booking");
        }

        booking.setStatus(BookingStatus.CONFIRMED);



        Booking saved = repository.save(booking);
        log.info("Confirmed booking id={}", id);

        // Observer Pattern: publish event — BookingNotificationListener will send
        // the confirmation email and award loyalty points.
        String hotelName = Objects.nonNull(saved.getRoom()) && Objects.nonNull(saved.getRoom().getHotel())
                ? saved.getRoom().getHotel().getName()
                : "the hotel";
        eventPublisher.publishEvent(new BookingConfirmedEvent(saved, hotelName));

        return mapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public BookingResponseDto complete(Long id, Long currentUserId, String role) {
        Booking booking = repository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));

        validateBookingAccess(booking, currentUserId, role);

        if (!Objects.equals(booking.getStatus(), BookingStatus.CONFIRMED)) {
            throw new BookingForbiddenActionException("Only CONFIRMED bookings can be marked as completed");
        }

        booking.setStatus(BookingStatus.COMPLETED);
        Booking saved = repository.save(booking);
        log.info("Completed booking id={}", id);
        return mapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public BookingResponseDto cancel(Long id, Long currentUserId, String role) {
        Booking booking = repository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));

        validateBookingAccess(booking, currentUserId, role);

        if (Objects.equals(booking.getStatus(), BookingStatus.CANCELLED)) {
            throw new BookingForbiddenActionException("This booking has already been cancelled");
        }
        if (Objects.equals(booking.getStatus(), BookingStatus.COMPLETED)) {
            throw new BookingForbiddenActionException("Cannot cancel a completed booking");
        }

        String hotelName = Objects.nonNull(booking.getRoom()) && Objects.nonNull(booking.getRoom().getHotel())
                ? booking.getRoom().getHotel().getName()
                : "the hotel";

        boolean hasPolicy = Objects.nonNull(booking.getCancellationPolicyName())
                && Objects.nonNull(booking.getCancellationDaysBeforeCheckin())
                && Objects.nonNull(booking.getCancellationRefundPercentage())
                && Objects.nonNull(booking.getCheckInDate())
                && Objects.nonNull(booking.getTotalPrice());

        if (hasPolicy) {
            long daysUntilCheckIn = ChronoUnit.DAYS.between(LocalDate.now(), booking.getCheckInDate());

            if (daysUntilCheckIn >= booking.getCancellationDaysBeforeCheckin()) {
                BigDecimal refund = BigDecimal.valueOf(booking.getTotalPrice())
                        .multiply(booking.getCancellationRefundPercentage())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                booking.setRefundAmount(refund);
                booking.setStatus(BookingStatus.CANCELLED);
                Booking saved = repository.save(booking);

                releaseInventory(saved.getRoom(), saved.getCheckInDate(), saved.getCheckOutDate());
                log.info("Booking id={} cancelled with refund={}", id, refund);

                // Observer Pattern: publish event — listener sends cancellation
                // email and refund email (if refund > 0).
                eventPublisher.publishEvent(new BookingCancelledEvent(saved, hotelName, refund));

                return mapper.toResponseDto(saved);

            } else {
                log.info("Booking id={} cancellation denied — policy violation, daysUntilCheckIn={}", id, daysUntilCheckIn);

                // Observer Pattern: publish event — listener sends policy-violation email.
                eventPublisher.publishEvent(new BookingCancellationDeniedEvent(booking, hotelName, daysUntilCheckIn));

                throw new BookingForbiddenActionException(
                        "Cancellation not allowed — check-in is in " + daysUntilCheckIn
                                + " days, but policy requires at least "
                                + booking.getCancellationDaysBeforeCheckin() + " days notice.");
            }
        }

        booking.setRefundAmount(BigDecimal.ZERO);
        booking.setStatus(BookingStatus.CANCELLED);
        Booking saved = repository.save(booking);

        releaseInventory(saved.getRoom(), saved.getCheckInDate(), saved.getCheckOutDate());
        log.info("Booking id={} cancelled with no policy — refundAmount=0", id);

        // Observer Pattern: publish event — listener sends cancellation email.
        eventPublisher.publishEvent(new BookingCancelledEvent(saved, hotelName, BigDecimal.ZERO));

        return mapper.toResponseDto(saved);
    }

    @Override
    public PagedResponse<BookingResponseDto> listByHotelIds(
            List<Long> hotelIds,
            BookingStatus status,
            Double minPrice,
            Double maxPrice,
            CancellationPolicyName policyName,
            Pageable pageable) {

        validatePriceRange(minPrice, maxPrice);

        Specification<Booking> spec = Specification
                .where(BookingSpecification.hasHotelIdIn(hotelIds))
                .and(BookingSpecification.hasStatus(status))
                .and(BookingSpecification.minPrice(minPrice))
                .and(BookingSpecification.maxPrice(maxPrice))
                .and(BookingSpecification.hasCancellationPolicyName(policyName));

        Page<Booking> page = repository.findAll(spec, pageable);

        return new PagedResponse<>(
                page.map(mapper::toResponseDto).getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @Override
    @Transactional
    public BookingResponseDto update(Long id, BookingRequestDto dto, Long currentUserId, String role) {
        Booking booking = repository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));

        validateBookingAccess(booking, currentUserId, role);

        if (Objects.equals(booking.getStatus(), BookingStatus.CANCELLED)
                || Objects.equals(booking.getStatus(), BookingStatus.COMPLETED)) {
            throw new BookingForbiddenActionException(
                    "Cannot update a booking with status: " + booking.getStatus());
        }

        Room oldRoom = booking.getRoom();
        LocalDate oldCheckIn = booking.getCheckInDate();
        LocalDate oldCheckOut = booking.getCheckOutDate();

        Room targetRoom = booking.getRoom();
        boolean roomChanged = Objects.isNull(booking.getRoom()) || !Objects.equals(booking.getRoom().getId(), dto.getRoomId());

        if (roomChanged) {
            targetRoom = roomRepository.findById(dto.getRoomId())
                    .orElseThrow(() -> new IllegalArgumentException("Room not found with id: " + dto.getRoomId()));

            if (!Boolean.TRUE.equals(targetRoom.getActive())) {
                throw new BookingForbiddenActionException(
                        "Room id=" + targetRoom.getId() + " is not available for booking. It may be under maintenance or temporarily unavailable.");
            }
        }

        validateBookingRequest(dto, targetRoom);

        BigDecimal recalculated = pricingRuleService.calculateTotalPrice(
                targetRoom.getId(),
                dto.getCheckInDate(),
                dto.getCheckOutDate()
        );

        boolean inventoryChanged = roomChanged
                || !Objects.equals(oldCheckIn, dto.getCheckInDate())
                || !Objects.equals(oldCheckOut, dto.getCheckOutDate());

        if (inventoryChanged) {
            releaseInventory(oldRoom, oldCheckIn, oldCheckOut);
            checkInventoryAvailable(targetRoom, dto.getCheckInDate(), dto.getCheckOutDate());
            reserveInventory(targetRoom, dto.getCheckInDate(), dto.getCheckOutDate());
        }

        booking.setRoom(targetRoom);
        booking.setCheckInDate(dto.getCheckInDate());
        booking.setCheckOutDate(dto.getCheckOutDate());
        booking.setNumberOfGuests(dto.getNumberOfGuests());
        booking.setTotalPrice(recalculated.doubleValue());

        log.info("Updated booking id={}", id);
        return mapper.toResponseDto(repository.save(booking));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Booking booking = repository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));

        if (!Objects.equals(booking.getStatus(), BookingStatus.CANCELLED)
                && !Objects.equals(booking.getStatus(), BookingStatus.COMPLETED)) {
            releaseInventory(booking.getRoom(), booking.getCheckInDate(), booking.getCheckOutDate());
        }

        List<Payment> payments = paymentRepository.findByBooking_Id(id);

        for (Payment payment : payments) {
            refundRepository.deleteAll(refundRepository.findByPaymentId(payment.getId()));
        }

        if (!payments.isEmpty()) {
            paymentRepository.deleteAll(payments);
        }

        repository.delete(booking);
        log.info("Deleted booking id={}", id);
    }

    // -----------------------------------------------------------------------
    // Private helpers — unchanged from original
    // -----------------------------------------------------------------------

    private void validateBookingRequest(BookingRequestDto dto, Room room) {
        if (dto.getCheckInDate() == null || dto.getCheckOutDate() == null) {
            throw new BookingForbiddenActionException("Check-in and check-out dates are required");
        }
        if (!dto.getCheckInDate().isBefore(dto.getCheckOutDate())) {
            throw new BookingForbiddenActionException("Check-out date must be after check-in date");
        }
        if (dto.getCheckInDate().isBefore(LocalDate.now())) {
            throw new BookingForbiddenActionException("Check-in date cannot be in the past");
        }
        if (dto.getNumberOfGuests() == null || dto.getNumberOfGuests() < 1) {
            throw new BookingForbiddenActionException("Number of guests must be at least 1");
        }
        if (room.getMaxCapacity() != null && dto.getNumberOfGuests() > room.getMaxCapacity()) {
            throw new BookingForbiddenActionException(
                    "Number of guests exceeds room capacity of " + room.getMaxCapacity());
        }
    }

    private void validateBookingAccess(Booking booking, Long currentUserId, String role) {
        if ("ADMIN".equals(role)) return;

        if ("USER".equals(role)) {
            if (!Objects.equals(booking.getUserId(), currentUserId)) {
                throw new BookingAccessDeniedException("You are not allowed to access this booking");
            }
            return;
        }

        if ("HOTEL_MANAGER".equals(role)) {
            Long managerId = booking.getRoom() != null && booking.getRoom().getHotel() != null
                    ? booking.getRoom().getHotel().getManagerId()
                    : null;
            if (!Objects.equals(managerId, currentUserId)) {
                throw new BookingAccessDeniedException("You are not allowed to access this booking");
            }
            return;
        }

        throw new BookingAccessDeniedException("You are not allowed to access this booking");
    }

    private void checkInventoryAvailable(Room room, LocalDate checkInDate, LocalDate checkOutDate) {
        LocalDate cursor = checkInDate;
        while (cursor.isBefore(checkOutDate)) {
            LocalDate night = cursor;
            Inventory inv = inventoryRepository.findByRoomIdAndDate(room.getId(), night)
                    .orElseThrow(() -> new BookingForbiddenActionException(
                            "No inventory set up for room id=" + room.getId() + " on " + night + ". Please contact the hotel."));
            if (!inv.hasAvailability(1)) {
                throw new BookingConflictException(
                        "No available rooms remain for room id=" + room.getId() + " on " + night + ". Please choose different dates.");
            }
            cursor = cursor.plusDays(1);
        }
    }

    private void reserveInventory(Room room, LocalDate checkInDate, LocalDate checkOutDate) {
        LocalDate cursor = checkInDate;
        while (cursor.isBefore(checkOutDate)) {
            LocalDate night = cursor;
            Inventory inv = inventoryRepository.findByRoomIdAndDate(room.getId(), night)
                    .orElseThrow(() -> new BookingForbiddenActionException(
                            "No inventory set up for room id=" + room.getId() + " on " + night + ". Please contact the hotel."));
            inv.reserve(1);
            inventoryRepository.save(inv);
            cursor = cursor.plusDays(1);
        }
        log.info("Reserved inventory for roomId={} from {} to {}", room.getId(), checkInDate, checkOutDate);
    }

    private void releaseInventory(Room room, LocalDate checkInDate, LocalDate checkOutDate) {
        if (room == null || checkInDate == null || checkOutDate == null) return;
        LocalDate cursor = checkInDate;
        while (cursor.isBefore(checkOutDate)) {
            LocalDate night = cursor;
            inventoryRepository.findByRoomIdAndDate(room.getId(), night).ifPresent(inv -> {
                inv.release(1);
                inventoryRepository.save(inv);
            });
            cursor = cursor.plusDays(1);
        }
        log.info("Released inventory for roomId={} from {} to {}", room.getId(), checkInDate, checkOutDate);
    }

    private void validatePriceRange(Double minPrice, Double maxPrice) {
        if (minPrice != null && minPrice < 0) throw new InvalidPriceRangeException("minPrice cannot be negative");
        if (maxPrice != null && maxPrice < 0) throw new InvalidPriceRangeException("maxPrice cannot be negative");
        if (minPrice != null && maxPrice != null && minPrice > maxPrice)
            throw new InvalidPriceRangeException("minPrice cannot be greater than maxPrice");
    }
}