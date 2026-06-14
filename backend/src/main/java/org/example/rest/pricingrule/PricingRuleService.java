package org.example.rest.pricingrule;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PricingRuleService {

    PricingRuleResponseDto create(Long hotelId, Long roomId, Long managerId, PricingRuleRequestDto dto);

    PricingRuleResponseDto getById(Long hotelId, Long roomId, Long id);

    List<PricingRuleResponseDto> listByRoom(Long hotelId, Long roomId);

    PricingRuleResponseDto update(Long hotelId, Long roomId, Long id, Long managerId, PricingRuleRequestDto dto);

    void delete(Long hotelId, Long roomId, Long id, Long managerId);

    BigDecimal calculateTotalPrice(Long roomId, LocalDate checkIn, LocalDate checkOut);
}