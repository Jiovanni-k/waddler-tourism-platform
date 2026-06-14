package org.example.rest.cancellationpolicy;

import org.example.rest.hotel.Hotel;
import org.springframework.stereotype.Component;

@Component
public class CancellationPolicyMapper {

    public CancellationPolicy toEntity(CancellationPolicyRequestDto dto, Hotel hotel) {
        return CancellationPolicy.builder()
                .hotel(hotel)
                .name(dto.getName())
                .description(dto.getDescription())
                .daysBeforeCheckin(dto.getDaysBeforeCheckin())
                .refundPercentage(dto.getRefundPercentage())
                .build();
    }

    public CancellationPolicyResponseDto toResponseDto(CancellationPolicy policy) {
        CancellationPolicyResponseDto dto = new CancellationPolicyResponseDto();
        dto.setId(policy.getId());
        dto.setHotelId(policy.getHotel().getId());
        dto.setHotelName(policy.getHotel().getName());
        dto.setName(policy.getName());
        dto.setDisplayName(policy.getName().getDisplayName());
        dto.setDescription(policy.getDescription());
        dto.setDaysBeforeCheckin(policy.getDaysBeforeCheckin());
        dto.setRefundPercentage(policy.getRefundPercentage());
        dto.setCreatedAt(policy.getCreatedAt());
        dto.setUpdatedAt(policy.getUpdatedAt());
        return dto;
    }
}