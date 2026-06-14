package org.example.rest.review;

import org.example.rest.PagedResponse;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    ReviewResponseDto addHotelReview(Long hotelId, Long reviewerUserId, ReviewRequestDto dto);

    ReviewResponseDto addEventReview(Long eventId, Long reviewerUserId, ReviewRequestDto dto);

    ReviewResponseDto updateMyReview(Long reviewId, Long reviewerUserId, ReviewRequestDto dto);

    void deleteMyReview(Long reviewId, Long reviewerUserId);

    PagedResponse<ReviewResponseDto> listByHotel(Long hotelId, ReviewSort sort, Pageable pageable);

    PagedResponse<ReviewResponseDto> listByEvent(Long eventId, ReviewSort sort, Pageable pageable);

    ReviewSummaryResponseDto summary(ReviewTargetType type, Long targetId);

    ReviewResponseDto hide(Long reviewId);
    ReviewResponseDto publish(Long reviewId);
    ReviewResponseDto report(Long reviewId, String reason);
    void addPhotoToReview(Long reviewId, String photoUrl, Long reviewerUserId);
}