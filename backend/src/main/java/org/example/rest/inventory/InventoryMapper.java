package org.example.rest.inventory;

import org.example.rest.room.Room;

public final class InventoryMapper {

    private InventoryMapper() {}

    public static Inventory toEntity(InventoryRequestDto dto, Room room) {
        if (dto == null) return null;
        return Inventory.builder()
                .room(room)
                .date(dto.getDate())
                .totalRooms(dto.getTotalRooms())
                .availableRooms(dto.getAvailableRooms())
                .build();
    }

    public static InventoryResponseDto toDto(Inventory inventory) {
        if (inventory == null) return null;

        String roomName = inventory.getRoom() != null
                ? inventory.getRoom().getName()
                : null;

        Long roomId = inventory.getRoom() != null
                ? inventory.getRoom().getId()
                : null;

        int booked = inventory.getTotalRooms() - inventory.getAvailableRooms();

        return InventoryResponseDto.builder()
                .id(inventory.getId())
                .roomId(roomId)
                .roomName(roomName)
                .date(inventory.getDate())
                .totalRooms(inventory.getTotalRooms())
                .availableRooms(inventory.getAvailableRooms())
                .bookedRooms(booked)
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}