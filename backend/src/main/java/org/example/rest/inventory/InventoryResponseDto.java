package org.example.rest.inventory;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class InventoryResponseDto {

    private Long id;
    private Long roomId;
    private String roomName;
    private LocalDate date;
    private Integer totalRooms;
    private Integer availableRooms;
    private Integer bookedRooms;
    private LocalDateTime updatedAt;
}