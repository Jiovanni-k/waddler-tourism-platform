package org.example.rest.pricingrule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rest.hotel.HotelRepository;
import org.example.rest.room.Room;
import org.example.rest.room.RoomNotFoundException;
import org.example.rest.room.RoomNotBelongToHotelException;
import org.example.rest.room.RoomRepository;
import org.example.rest.security.SecurityUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PricingRuleServiceImpl implements PricingRuleService {

    private final PricingRuleRepository repository;
    private final PricingRuleMapper mapper;
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    @Override
    @Transactional
    public PricingRuleResponseDto create(Long hotelId, Long roomId, Long managerId,
                                         PricingRuleRequestDto dto) {
        Room room = findAndValidateRoom(hotelId, roomId);
        verifyOwnership(room, managerId);

        if (dto.getEndDate().isBefore(dto.getStartDate()))
            throw new IllegalArgumentException("End date must not be before start date");

        if (repository.existsByRoom_IdAndName(roomId, dto.getName()))
            throw new DuplicatePricingRuleException(dto.getName(), roomId);

        PricingRule rule = mapper.toEntity(dto, room);
        PricingRule saved = repository.save(rule);
        log.info("Created pricing rule id={} '{}' for roomId={} by managerId={}",
                saved.getId(), saved.getName(), roomId, managerId);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PricingRuleResponseDto getById(Long hotelId, Long roomId, Long id) {
        findAndValidateRoom(hotelId, roomId);
        PricingRule rule = repository.findById(id)
                .orElseThrow(() -> new PricingRuleNotFoundException(id));
        if (!rule.getRoom().getId().equals(roomId))
            throw new PricingRuleNotFoundException(id);
        return mapper.toDto(rule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PricingRuleResponseDto> listByRoom(Long hotelId, Long roomId) {
        findAndValidateRoom(hotelId, roomId);
        return repository.findByRoom_Id(roomId)
                .stream().map(mapper::toDto).toList();
    }

    @Override
    @Transactional
    public PricingRuleResponseDto update(Long hotelId, Long roomId, Long id,
                                         Long managerId, PricingRuleRequestDto dto) {
        Room room = findAndValidateRoom(hotelId, roomId);
        verifyOwnership(room, managerId);

        PricingRule rule = repository.findById(id)
                .orElseThrow(() -> new PricingRuleNotFoundException(id));
        if (!rule.getRoom().getId().equals(roomId))
            throw new PricingRuleNotFoundException(id);

        if (dto.getEndDate().isBefore(dto.getStartDate()))
            throw new IllegalArgumentException("End date must not be before start date");

        if (!rule.getName().equals(dto.getName()) &&
                repository.existsByRoom_IdAndNameAndIdNot(roomId, dto.getName(), id))
            throw new DuplicatePricingRuleException(dto.getName(), roomId);

        rule.setName(dto.getName());
        rule.setDescription(dto.getDescription());
        rule.setStartDate(dto.getStartDate());
        rule.setEndDate(dto.getEndDate());
        rule.setPricePerNight(dto.getPricePerNight());
        rule.setPriority(dto.getPriority() != null ? dto.getPriority() : rule.getPriority());
        rule.setActive(dto.getActive() != null ? dto.getActive() : rule.getActive());

        log.info("Updated pricing rule id={} for roomId={} by managerId={}", id, roomId, managerId);
        return mapper.toDto(repository.save(rule));
    }

    @Override
    @Transactional
    public void delete(Long hotelId, Long roomId, Long id, Long managerId) {
        Room room = findAndValidateRoom(hotelId, roomId);
        verifyOwnership(room, managerId);

        PricingRule rule = repository.findById(id)
                .orElseThrow(() -> new PricingRuleNotFoundException(id));
        if (!rule.getRoom().getId().equals(roomId))
            throw new PricingRuleNotFoundException(id);

        repository.delete(rule);
        log.info("Deleted pricing rule id={} for roomId={} by managerId={}", id, roomId, managerId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalPrice(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn))
            throw new IllegalArgumentException("Invalid check-in/check-out dates");

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));

        List<PricingRule> rules = repository.findActiveRulesForDateRange(roomId, checkIn, checkOut);

        BigDecimal total = BigDecimal.ZERO;

        LocalDate date = checkIn;
        while (date.isBefore(checkOut)) {
            final LocalDate currentDate = date;
            BigDecimal priceForDay = rules.stream()
                    .filter(r -> r.appliesTo(currentDate))
                    .findFirst()
                    .map(PricingRule::getPricePerNight)
                    .orElse(room.getBasePrice());

            total = total.add(priceForDay);
            date = date.plusDays(1);
        }

        log.info("Calculated total price {} for roomId={} checkIn={} checkOut={}",
                total, roomId, checkIn, checkOut);
        return total;
    }

    private Room findAndValidateRoom(Long hotelId, Long roomId) {
        if (!hotelRepository.existsById(hotelId))
            throw new org.example.rest.hotel.HotelNotFoundException(hotelId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));
        if (!room.getHotel().getId().equals(hotelId))
            throw new RoomNotBelongToHotelException(roomId, hotelId);
        return room;
    }

    private void verifyOwnership(Room room, Long managerId) {
        String role = SecurityUtil.getCurrentUserRole();
        if ("ADMIN".equals(role)) return;
        if (!room.getHotel().getManagerId().equals(managerId))
            throw new AccessDeniedException(
                    "You do not have permission to manage pricing rules for this room");
    }
}