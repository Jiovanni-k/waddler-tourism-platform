package org.example.rest.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rest.PagedResponse;
import org.example.rest.booking.BookingRepository;
import org.example.rest.event.EventNotFoundException;
import org.example.rest.event.EventRepository;
import org.example.rest.eventreservation.EventReservationRepository;
import org.example.rest.hotel.HotelNotFoundException;
import org.example.rest.hotel.HotelRepository;
import org.example.rest.security.SecurityUtil;
import org.example.rest.security.user.AppUser;
import org.example.rest.security.user.UserRepository;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository repo;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final EventReservationRepository eventReservationRepository;
    private final HotelRepository hotelRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public ReviewResponseDto addHotelReview(Long hotelId, Long reviewerUserId, ReviewRequestDto dto) {
        if (!hotelRepository.existsById(hotelId)) {
            throw new HotelNotFoundException(hotelId);
        }
        ensureCanReviewHotel(hotelId, reviewerUserId);
        return createForTarget(ReviewTargetType.HOTEL, hotelId, reviewerUserId, dto);
    }

    @Override
    @Transactional
    public ReviewResponseDto addEventReview(Long eventId, Long reviewerUserId, ReviewRequestDto dto) {
        if (!eventRepository.existsById(eventId)) {
            throw new EventNotFoundException(eventId);
        }
        ensureCanReviewEvent(eventId, reviewerUserId);
        return createForTarget(ReviewTargetType.EVENT, eventId, reviewerUserId, dto);
    }

    private ReviewResponseDto createForTarget(ReviewTargetType type, Long targetId,
                                              Long reviewerUserId, ReviewRequestDto dto) {
        if (repo.existsByReviewerUserIdAndTargetTypeAndTargetId(reviewerUserId, type, targetId)) {
            throw new AlreadyReviewedException();
        }

        AppUser reviewer = userRepository.findById(reviewerUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + reviewerUserId));

        Review r = ReviewMapper.toEntity(dto);
        r.setReviewer(reviewer);
        r.setTargetType(type);
        r.setTargetId(targetId);

        Review saved = repo.save(r);
        log.info("Created review id={} for {}:{} by userId={}", saved.getId(), type, targetId, reviewerUserId);

        ReviewResponseDto out = ReviewMapper.toDto(saved);
        out.setCanEdit(true);
        return out;
    }

    @Override
    @Transactional
    public ReviewResponseDto updateMyReview(Long reviewId, Long reviewerUserId, ReviewRequestDto dto) {
        Review r = findOrThrow(reviewId);

        if (!r.getReviewerUserId().equals(reviewerUserId)) {
            throw new ReviewNotAllowedException("You can only update your own review");
        }
        if (r.getStatus() == ReviewStatus.HIDDEN) {
            throw new ReviewNotAllowedException("Cannot update a hidden review");
        }

        ReviewMapper.apply(dto, r);
        Review saved = repo.save(r);
        log.info("Updated review id={} by userId={}", reviewId, reviewerUserId);

        ReviewResponseDto out = ReviewMapper.toDto(saved);
        out.setCanEdit(true);
        return out;
    }

    @Override
    @Transactional
    public void deleteMyReview(Long reviewId, Long reviewerUserId) {
        Review r = findOrThrow(reviewId);
        String role = SecurityUtil.getCurrentUserRole();
        if (!"ADMIN".equals(role) && !r.getReviewerUserId().equals(reviewerUserId)) {
            throw new ReviewNotAllowedException("You can only delete your own review");
        }
        repo.delete(r);
        log.info("Deleted review id={} by userId={}", reviewId, reviewerUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponseDto> listByHotel(Long hotelId, ReviewSort sort, Pageable pageable) {
        if (!hotelRepository.existsById(hotelId)) {
            throw new HotelNotFoundException(hotelId);
        }
        return listByTarget(ReviewTargetType.HOTEL, hotelId, sort, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponseDto> listByEvent(Long eventId, ReviewSort sort, Pageable pageable) {
        if (!eventRepository.existsById(eventId)) {
            throw new EventNotFoundException(eventId);
        }
        return listByTarget(ReviewTargetType.EVENT, eventId, sort, pageable);
    }

    private PagedResponse<ReviewResponseDto> listByTarget(ReviewTargetType type, Long targetId,
                                                          ReviewSort sort, Pageable pageable) {
        Pageable effective = applySort(sort, pageable);

        Specification<Review> spec = Specification
                .where(ReviewSpecifications.target(type, targetId))
                .and(ReviewSpecifications.status(ReviewStatus.PUBLISHED));

        Page<Review> page = repo.findAll(spec, effective);

        return new PagedResponse<>(
                page.map(ReviewMapper::toDto).getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    private Pageable applySort(ReviewSort sort, Pageable pageable) {
        if (sort == null) return pageable;
        Sort s = switch (sort) {
            case NEWEST  -> Sort.by("createdAt").descending();
            case OLDEST  -> Sort.by("createdAt").ascending();
            case HIGHEST -> Sort.by("rating").descending().and(Sort.by("createdAt").descending());
            case LOWEST  -> Sort.by("rating").ascending().and(Sort.by("createdAt").descending());
        };
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), s);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewSummaryResponseDto summary(ReviewTargetType type, Long targetId) {
        if (type == ReviewTargetType.HOTEL && !hotelRepository.existsById(targetId)) {
            throw new HotelNotFoundException(targetId);
        }
        if (type == ReviewTargetType.EVENT && !eventRepository.existsById(targetId)) {
            throw new EventNotFoundException(targetId);
        }

        long count = repo.countByTargetTypeAndTargetIdAndStatus(type, targetId, ReviewStatus.PUBLISHED);

        Map<Integer, Long> breakdown = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            breakdown.put(i, repo.countByTargetTypeAndTargetIdAndStatusAndRating(
                    type, targetId, ReviewStatus.PUBLISHED, i));
        }

        double avg = 0.0;
        if (count > 0) {
            long weightedSum = breakdown.entrySet().stream()
                    .mapToLong(e -> (long) e.getKey() * e.getValue())
                    .sum();
            avg = (double) weightedSum / count;
        }

        return new ReviewSummaryResponseDto(avg, count, breakdown);
    }

    @Override
    @Transactional
    public ReviewResponseDto hide(Long reviewId) {
        Review r = findOrThrow(reviewId);
        if (r.getStatus() == ReviewStatus.HIDDEN) {
            throw new IllegalArgumentException("Review is already hidden");
        }
        r.setStatus(ReviewStatus.HIDDEN);
        log.info("Hidden review id={}", reviewId);
        return ReviewMapper.toDto(repo.save(r));
    }

    @Override
    @Transactional
    public ReviewResponseDto publish(Long reviewId) {
        Review r = findOrThrow(reviewId);
        if (r.getStatus() == ReviewStatus.PUBLISHED) {
            throw new IllegalArgumentException("Review is already published");
        }
        r.setStatus(ReviewStatus.PUBLISHED);
        log.info("Published review id={}", reviewId);
        return ReviewMapper.toDto(repo.save(r));
    }

    @Override
    @Transactional
    public ReviewResponseDto report(Long reviewId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Reason is required");
        }
        Review r = findOrThrow(reviewId);
        r.setStatus(ReviewStatus.REPORTED);
        log.info("Reported review id={} reason={}", reviewId, reason);
        return ReviewMapper.toDto(repo.save(r));
    }

    @Override
    @Transactional
    public void addPhotoToReview(Long reviewId, String photoUrl, Long reviewerUserId) {
        Review r = findOrThrow(reviewId);
        String role = SecurityUtil.getCurrentUserRole();
        if (!"ADMIN".equals(role) && !r.getReviewerUserId().equals(reviewerUserId)) {
            throw new ReviewNotAllowedException("You can only add photos to your own review");
        }
        r.getPhotos().add(photoUrl);
        repo.save(r);
        log.info("Added photo to review id={} by userId={}", reviewId, reviewerUserId);
    }

    private Review findOrThrow(Long reviewId) {
        return repo.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));
    }

    private void ensureCanReviewHotel(Long hotelId, Long userId) {
        boolean hasCompletedBooking = bookingRepository
                .existsCompletedBookingByUserAndHotel(userId, hotelId);
        if (!hasCompletedBooking) {
            throw new ReviewNotEligibleException(
                    "You can only review a hotel after completing a stay there.");
        }
    }

    private void ensureCanReviewEvent(Long eventId, Long userId) {
        boolean hasAttendedReservation = eventReservationRepository
                .existsAttendedReservationByUserAndEvent(userId, eventId);
        if (!hasAttendedReservation) {
            throw new ReviewNotEligibleException(
                    "You can only review an event after attending it.");
        }
    }
}