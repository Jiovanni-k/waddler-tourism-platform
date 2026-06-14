package org.example.rest.cancellationpolicy;

import org.example.rest.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface CancellationPolicyService {

    CancellationPolicyResponseDto create(Long hotelId, Long managerId, CancellationPolicyRequestDto dto);

    CancellationPolicyResponseDto getById(Long hotelId, Long id);

    PagedResponse<CancellationPolicyResponseDto> list(
            Long hotelId,
            CancellationPolicyName name,
            Integer minDays,
            Integer maxDays,
            BigDecimal minRefund,
            BigDecimal maxRefund,
            Pageable pageable
    );

    CancellationPolicyResponseDto update(Long hotelId, Long id, Long managerId, CancellationPolicyRequestDto dto);

    void delete(Long hotelId, Long id, Long managerId);
}