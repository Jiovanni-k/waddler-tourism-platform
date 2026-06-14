package org.example.rest.inventory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rest.PagedResponse;
import org.example.rest.hotel.HotelNotFoundException;
import org.example.rest.hotel.HotelRepository;
import org.example.rest.room.Room;
import org.example.rest.room.RoomNotFoundException;
import org.example.rest.room.RoomNotBelongToHotelException;
import org.example.rest.room.RoomRepository;
import org.example.rest.security.SecurityUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    @Override
    @Transactional
    public InventoryResponseDto create(Long hotelId, Long roomId, Long managerId,
                                       InventoryRequestDto dto) {
        Room room = findAndValidateRoom(hotelId, roomId);
        verifyOwnership(room, managerId);

        if (inventoryRepository.existsByRoomIdAndDate(roomId, dto.getDate()))
            throw new IllegalArgumentException(
                    "Inventory already exists for roomId=" + roomId + " on date=" + dto.getDate());

        if (dto.getAvailableRooms() > dto.getTotalRooms())
            throw new IllegalArgumentException("Available rooms cannot exceed total rooms");

        Inventory inventory = InventoryMapper.toEntity(dto, room);
        Inventory saved = inventoryRepository.save(inventory);
        log.info("Created inventory id={} for roomId={} on date={}", saved.getId(), roomId, dto.getDate());
        return InventoryMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponseDto getById(Long id) {
        return InventoryMapper.toDto(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponseDto getByRoomAndDate(Long roomId, LocalDate date) {
        return InventoryMapper.toDto(inventoryRepository.findByRoomIdAndDate(roomId, date)
                .orElseThrow(() -> new InventoryNotFoundException(roomId, date)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponseDto> getByRoom(Long roomId) {
        return inventoryRepository.findByRoomId(roomId)
                .stream().map(InventoryMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponseDto> getByRoomAndDateRange(Long roomId,
                                                            LocalDate dateFrom,
                                                            LocalDate dateTo) {
        if (Objects.nonNull(dateFrom) && Objects.nonNull(dateTo) && dateTo.isBefore(dateFrom))
            throw new IllegalArgumentException("dateTo must not be before dateFrom");
        return inventoryRepository.findByRoomIdAndDateBetween(roomId, dateFrom, dateTo)
                .stream().map(InventoryMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<InventoryResponseDto> list(Long roomId, LocalDate dateFrom,
                                                    LocalDate dateTo, Pageable pageable) {
        if (Objects.nonNull(dateFrom) && Objects.nonNull(dateTo) && dateTo.isBefore(dateFrom))
            throw new IllegalArgumentException("dateTo must not be before dateFrom");

        List<InventoryResponseDto> all = (dateFrom != null && dateTo != null)
                ? inventoryRepository.findByRoomIdAndDateBetween(roomId, dateFrom, dateTo)
                .stream().map(InventoryMapper::toDto).toList()
                : inventoryRepository.findByRoomId(roomId)
                .stream().map(InventoryMapper::toDto).toList();

        int start = (int) pageable.getOffset();
        int end   = Math.min(start + pageable.getPageSize(), all.size());
        List<InventoryResponseDto> content = start >= all.size()
                ? List.of() : all.subList(start, end);

        return new PagedResponse<>(
                content,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                all.size(),
                (int) Math.ceil((double) all.size() / pageable.getPageSize()),
                end >= all.size()
        );
    }


    @Override
    @Transactional
    public InventoryResponseDto update(Long hotelId, Long roomId, Long id,
                                       Long managerId, InventoryRequestDto dto) {
        Room room = findAndValidateRoom(hotelId, roomId);
        verifyOwnership(room, managerId);

        Inventory inventory = findOrThrow(id);

        if (!inventory.getRoom().getId().equals(roomId))
            throw new InventoryNotFoundException(id);

        if (dto.getAvailableRooms() > dto.getTotalRooms())
            throw new IllegalArgumentException("Available rooms cannot exceed total rooms");

        inventory.setTotalRooms(dto.getTotalRooms());
        inventory.setAvailableRooms(dto.getAvailableRooms());

        log.info("Updated inventory id={}", id);
        return InventoryMapper.toDto(inventoryRepository.save(inventory));
    }


    @Override
    @Transactional
    public InventoryResponseDto reserve(Long roomId, LocalDate date, int count) {
        if (count <= 0)
            throw new IllegalArgumentException("Reserve count must be greater than 0");

        Inventory inventory = inventoryRepository.findByRoomIdAndDate(roomId, date)
                .orElseThrow(() -> new InventoryNotFoundException(roomId, date));

        inventory.reserve(count);
        log.info("Reserved {} room(s) for roomId={} on date={}", count, roomId, date);
        return InventoryMapper.toDto(inventoryRepository.save(inventory));
    }

    @Override
    @Transactional
    public InventoryResponseDto release(Long roomId, LocalDate date, int count) {
        if (count <= 0)
            throw new IllegalArgumentException("Release count must be greater than 0");

        Inventory inventory = inventoryRepository.findByRoomIdAndDate(roomId, date)
                .orElseThrow(() -> new InventoryNotFoundException(roomId, date));

        inventory.release(count);
        log.info("Released {} room(s) for roomId={} on date={}", count, roomId, date);
        return InventoryMapper.toDto(inventoryRepository.save(inventory));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Inventory inventory = findOrThrow(id);
        inventoryRepository.delete(inventory);
        log.info("Deleted inventory id={}", id);
    }

    private Inventory findOrThrow(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException(id));
    }

    private Room findAndValidateRoom(Long hotelId, Long roomId) {
        if (!hotelRepository.existsById(hotelId))
            throw new HotelNotFoundException(hotelId);
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
                    "You do not have permission to manage inventory for this room");
    }
}