package org.example.rest.room;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rest.PagedResponse;
import org.example.rest.amenity.Amenity;
import org.example.rest.amenity.AmenityDto;
import org.example.rest.amenity.AmenityMapper;
import org.example.rest.amenity.AmenityNotFoundException;
import org.example.rest.amenity.AmenityRepository;
import org.example.rest.booking.Booking;
import org.example.rest.booking.BookingRepository;
import org.example.rest.booking.BookingStatus;
import org.example.rest.cancellationpolicy.CancellationPolicyNotBelongToHotelException;
import org.example.rest.cancellationpolicy.CancellationPolicyRepository;
import org.example.rest.hotel.Hotel;
import org.example.rest.hotel.HotelNotFoundException;
import org.example.rest.hotel.HotelRepository;
import org.example.rest.notification.EmailService;
import org.example.rest.security.SecurityUtil;
import org.example.rest.security.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;
    private final HotelRepository hotelRepository;
    private final AmenityRepository amenityRepository;
    private final CancellationPolicyRepository cancellationPolicyRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public RoomResponseDto create(Long hotelId, Long managerId, RoomRequestDto dto) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException(hotelId));

        verifyOwnership(hotel, managerId);

        if (dto.getName() != null && roomRepository.existsByHotel_IdAndName(hotelId, dto.getName()))
            throw new DuplicateRoomException(dto.getName(), hotelId);

        if (dto.getCancellationPolicyId() != null &&
                !cancellationPolicyRepository.existsByIdAndHotel_Id(dto.getCancellationPolicyId(), hotelId))
            throw new CancellationPolicyNotBelongToHotelException(dto.getCancellationPolicyId(), hotelId);

        Set<Amenity> amenities = resolveAmenities(dto.getAmenityIds());
        Room room = roomMapper.toEntity(dto, hotel, amenities);
        Room saved = roomRepository.save(room);
        log.info("Created room id={} name='{}' for hotelId={} by managerId={}",
                saved.getId(), saved.getName(), hotelId, managerId);
        return roomMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponseDto getById(Long hotelId, Long id) {
        if (!hotelRepository.existsById(hotelId))
            throw new HotelNotFoundException(hotelId);
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException(id));
        if (!room.getHotel().getId().equals(hotelId))
            throw new RoomNotBelongToHotelException(id, hotelId);
        return roomMapper.toResponseDto(room);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<RoomResponseDto> list(
            Long hotelId, String name, RoomType roomType, Boolean active,
            String bedType, Integer minCapacity, BigDecimal minPrice,
            BigDecimal maxPrice, Long cancellationPolicyId, Pageable pageable) {

        if (!hotelRepository.existsById(hotelId))
            throw new HotelNotFoundException(hotelId);

        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0)
            throw new IllegalArgumentException("minPrice must not be greater than maxPrice");

        RoomPricingStrategy pricingStrategy;
        if (minPrice != null && maxPrice != null) {
            pricingStrategy = new RangePricingStrategy();
        } else if (maxPrice != null) {
            pricingStrategy = new BudgetPricingStrategy();
        } else if (minPrice != null) {
            pricingStrategy = new LuxuryPricingStrategy();
        } else {
            pricingStrategy = new NoPricingStrategy();
        }

        Specification<Room> spec = Specification
                .where(RoomSpecification.hasHotelId(hotelId))
                .and(RoomSpecification.hasNameContaining(name))
                .and(RoomSpecification.hasRoomType(roomType))
                .and(RoomSpecification.isActive(active))
                .and(RoomSpecification.hasBedType(bedType))
                .and(RoomSpecification.minCapacity(minCapacity))
                .and(pricingStrategy.apply(minPrice, maxPrice))
                .and(RoomSpecification.hasCancellationPolicy(cancellationPolicyId));

        Page<Room> page = roomRepository.findAll(spec, pageable);

        return new PagedResponse<>(
                page.map(roomMapper::toResponseDto).getContent(),
                page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isLast()
        );
    }

    @Override
    @Transactional
    public RoomResponseDto update(Long hotelId, Long id, Long managerId, RoomRequestDto dto) {
        Room room = findAndValidateOwnership(hotelId, id);
        verifyOwnership(room.getHotel(), managerId);

        if (dto.getName() != null && !dto.getName().equals(room.getName()) &&
                roomRepository.existsByHotel_IdAndName(hotelId, dto.getName()))
            throw new DuplicateRoomException(dto.getName(), hotelId);

        if (dto.getCancellationPolicyId() != null &&
                !cancellationPolicyRepository.existsByIdAndHotel_Id(dto.getCancellationPolicyId(), hotelId))
            throw new CancellationPolicyNotBelongToHotelException(dto.getCancellationPolicyId(), hotelId);

        room.setName(dto.getName());
        room.setRoomType(dto.getRoomType());
        room.setDescription(dto.getDescription());
        room.setMaxCapacity(dto.getMaxCapacity());
        room.setTotalRooms(dto.getTotalRooms());
        room.setBasePrice(dto.getBasePrice());
        room.setBedType(dto.getBedType());
        room.setCancellationPolicyId(dto.getCancellationPolicyId());
        if (dto.getActive() != null) room.setActive(dto.getActive());
        if (dto.getAmenityIds() != null) room.setAmenities(resolveAmenities(dto.getAmenityIds()));

        log.info("Updated room id={} for hotelId={} by managerId={}", id, hotelId, managerId);
        return roomMapper.toResponseDto(roomRepository.save(room));
    }

    @Override
    @Transactional
    public RoomResponseDto activate(Long hotelId, Long id, Long managerId) {
        Room room = findAndValidateOwnership(hotelId, id);
        verifyOwnership(room.getHotel(), managerId);

        if (Boolean.TRUE.equals(room.getActive()))
            throw new IllegalStateException("Room id=" + id + " is already active");

        room.setActive(true);
        log.info("Activated room id={} by managerId={}", id, managerId);
        return roomMapper.toResponseDto(roomRepository.save(room));
    }

    @Override
    @Transactional
    public RoomResponseDto deactivate(Long hotelId, Long id, Long managerId) {
        Room room = findAndValidateOwnership(hotelId, id);
        verifyOwnership(room.getHotel(), managerId);

        if (Boolean.FALSE.equals(room.getActive()))
            throw new IllegalStateException("Room id=" + id + " is already inactive");

        room.setActive(false);
        Room saved = roomRepository.save(room);
        log.info("Deactivated room id={} by managerId={}", id, managerId);

        List<Booking> activeBookings = bookingRepository.findByRoom_Id(id).stream()
                .filter(b -> b.getStatus() == BookingStatus.PENDING
                        || b.getStatus() == BookingStatus.CONFIRMED)
                .toList();

        if (!activeBookings.isEmpty()) {
            log.info("Room id={} deactivated — cancelling and notifying {} users with active bookings",
                    id, activeBookings.size());
            String managerEmail = userRepository.findById(room.getHotel().getManagerId())
                    .map(u -> u.getEmail()).orElse("support@waddler.com");
            String hotelName = room.getHotel().getName();
            String roomName = room.getName();

            activeBookings.forEach(booking -> {
                booking.setStatus(BookingStatus.CANCELLED);
                bookingRepository.save(booking);

                userRepository.findById(booking.getUserId()).ifPresent(user -> {
                    try {
                        emailService.sendRoomDeactivated(
                                user.getEmail(), user.getFirstName(),
                                booking, hotelName, roomName, managerEmail);
                    } catch (Exception e) {
                        log.warn("Room deactivation email failed for userId={} — {}",
                                booking.getUserId(), e.getMessage());
                    }
                });
            });
        }

        return roomMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AmenityDto> getRoomAmenities(Long hotelId, Long roomId) {
        Room room = findAndValidateOwnership(hotelId, roomId);
        return room.getAmenities().stream()
                .map(AmenityMapper::toDto)
                .toList();
    }

    private void verifyOwnership(Hotel hotel, Long managerId) {
        String role = SecurityUtil.getCurrentUserRole();
        if ("ADMIN".equals(role)) return;
        if (!hotel.getManagerId().equals(managerId))
            throw new AccessDeniedException(
                    "You do not have permission to manage rooms for hotel id=" + hotel.getId());
    }

    private Room findAndValidateOwnership(Long hotelId, Long id) {
        if (!hotelRepository.existsById(hotelId))
            throw new HotelNotFoundException(hotelId);
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException(id));
        if (!room.getHotel().getId().equals(hotelId))
            throw new RoomNotBelongToHotelException(id, hotelId);
        return room;
    }

    private Set<Amenity> resolveAmenities(Set<Long> amenityIds) {
        if (amenityIds == null || amenityIds.isEmpty()) return new HashSet<>();
        Set<Amenity> amenities = new HashSet<>();
        for (Long amenityId : amenityIds) {
            amenities.add(amenityRepository.findById(amenityId)
                    .orElseThrow(() -> new AmenityNotFoundException(amenityId)));
        }
        return amenities;
    }
}