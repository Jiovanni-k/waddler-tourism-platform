package org.example.rest.pricingrule;

import org.example.rest.room.Room;
import org.springframework.stereotype.Component;

@Component
public class PricingRuleMapper {

    public PricingRule toEntity(PricingRuleRequestDto dto, Room room) {
        return PricingRule.builder()
                .room(room)
                .name(dto.getName())
                .description(dto.getDescription())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .pricePerNight(dto.getPricePerNight())
                .priority(dto.getPriority() != null ? dto.getPriority() : 0)
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();
    }

    public PricingRuleResponseDto toDto(PricingRule rule) {
        return PricingRuleResponseDto.builder()
                .id(rule.getId())
                .roomId(rule.getRoom().getId())
                .roomName(rule.getRoom().getName())
                .hotelId(rule.getRoom().getHotel().getId())
                .hotelName(rule.getRoom().getHotel().getName())
                .name(rule.getName())
                .description(rule.getDescription())
                .startDate(rule.getStartDate())
                .endDate(rule.getEndDate())
                .pricePerNight(rule.getPricePerNight())
                .priority(rule.getPriority())
                .active(rule.getActive())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }
}