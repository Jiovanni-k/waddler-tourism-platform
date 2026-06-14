package org.example.rest.booking;

import org.example.rest.PagedResponse;
import org.example.rest.cancellationpolicy.CancellationPolicyName;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookingService {

    BookingResponseDto create(BookingRequestDto dto, Long userId);

    BookingResponseDto getById(Long id, Long currentUserId, String role);

    PagedResponse<BookingResponseDto> list(
            Long userId,
            BookingStatus status,
            Long hotelId,
            Double minPrice,
            Double maxPrice,
            CancellationPolicyName policyName,
            Pageable pageable
    );

    PagedResponse<BookingResponseDto> listByHotelIds(
            List<Long> hotelIds,
            BookingStatus status,
            Double minPrice,
            Double maxPrice,
            CancellationPolicyName policyName,
            Pageable pageable
    );

    BookingResponseDto update(Long id, BookingRequestDto dto, Long currentUserId, String role);
    BookingResponseDto confirm(Long id, Long currentUserId, String role);
    BookingResponseDto complete(Long id, Long currentUserId, String role);
    BookingResponseDto cancel(Long id, Long currentUserId, String role);

    void delete(Long id);
}