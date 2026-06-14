package org.example.rest.review;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private EventReservationRepository eventReservationRepository;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private AppUser testUser;
    private Review testReview;
    private ReviewRequestDto testDto;

    @BeforeEach
    void setUp() {
        testUser = new AppUser();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");

        testReview = new Review();
        testReview.setId(1L);
        testReview.setReviewer(testUser);
        testReview.setTargetType(ReviewTargetType.HOTEL);
        testReview.setTargetId(100L);
        testReview.setRating(5);
        testReview.setComment("Great hotel!");
        testReview.setStatus(ReviewStatus.PUBLISHED);
        testReview.setCreatedAt(LocalDateTime.now());
        testReview.setUpdatedAt(LocalDateTime.now());

        testDto = new ReviewRequestDto();
        testDto.setRating(5);
        testDto.setComment("Great experience!");
        testDto.setPhotos(new ArrayList<>());
    }

    @Test
    void testAddHotelReview_WithValidData_CreatesReview() {
        when(hotelRepository.existsById(100L)).thenReturn(true);
        when(bookingRepository.existsCompletedBookingByUserAndHotel(1L, 100L)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(reviewRepository.existsByReviewerUserIdAndTargetTypeAndTargetId(1L, ReviewTargetType.HOTEL, 100L))
                .thenReturn(false);

        Review savedReview = new Review();
        savedReview.setId(1L);
        savedReview.setReviewer(testUser);
        savedReview.setTargetType(ReviewTargetType.HOTEL);
        savedReview.setTargetId(100L);
        savedReview.setRating(5);
        savedReview.setComment("Great experience!");
        savedReview.setPhotos(new ArrayList<>());
        savedReview.setStatus(ReviewStatus.PUBLISHED);
        savedReview.setCreatedAt(LocalDateTime.now());
        savedReview.setUpdatedAt(LocalDateTime.now());

        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        ReviewResponseDto result = reviewService.addHotelReview(100L, 1L, testDto);

        assertNotNull(result);
        assertEquals(5, result.getRating());
        assertEquals("Great experience!", result.getComment());
        assertEquals(ReviewTargetType.HOTEL, result.getTargetType());
        assertEquals("John Doe", result.getReviewerName()); // ✅ Now this will work
        assertTrue(result.getCanEdit());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void testAddHotelReview_WithNonExistentHotel_ThrowsException() {
        when(hotelRepository.existsById(999L)).thenReturn(false);

        assertThrows(HotelNotFoundException.class,
                () -> reviewService.addHotelReview(999L, 1L, testDto));

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void testAddHotelReview_WithoutCompletedBooking_ThrowsException() {
        when(hotelRepository.existsById(100L)).thenReturn(true);
        when(bookingRepository.existsCompletedBookingByUserAndHotel(1L, 100L)).thenReturn(false);

        assertThrows(ReviewNotEligibleException.class,
                () -> reviewService.addHotelReview(100L, 1L, testDto));

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void testAddHotelReview_WithAlreadyReviewedHotel_ThrowsException() {
        when(hotelRepository.existsById(100L)).thenReturn(true);
        when(bookingRepository.existsCompletedBookingByUserAndHotel(1L, 100L)).thenReturn(true);
        when(reviewRepository.existsByReviewerUserIdAndTargetTypeAndTargetId(1L, ReviewTargetType.HOTEL, 100L))
                .thenReturn(true);

        assertThrows(AlreadyReviewedException.class,
                () -> reviewService.addHotelReview(100L, 1L, testDto));

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void testAddHotelReview_WithNonExistentUser_ThrowsException() {
        when(hotelRepository.existsById(100L)).thenReturn(true);
        when(bookingRepository.existsCompletedBookingByUserAndHotel(1L, 100L)).thenReturn(true);
        when(reviewRepository.existsByReviewerUserIdAndTargetTypeAndTargetId(1L, ReviewTargetType.HOTEL, 100L))
                .thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> reviewService.addHotelReview(100L, 1L, testDto));

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void testAddEventReview_WithValidData_CreatesReview() {
        when(eventRepository.existsById(200L)).thenReturn(true);
        when(eventReservationRepository.existsAttendedReservationByUserAndEvent(1L, 200L)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(reviewRepository.existsByReviewerUserIdAndTargetTypeAndTargetId(1L, ReviewTargetType.EVENT, 200L))
                .thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        ReviewResponseDto result = reviewService.addEventReview(200L, 1L, testDto);

        assertNotNull(result);
        assertEquals(5, result.getRating());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void testAddEventReview_WithNonExistentEvent_ThrowsException() {
        when(eventRepository.existsById(999L)).thenReturn(false);

        assertThrows(EventNotFoundException.class,
                () -> reviewService.addEventReview(999L, 1L, testDto));

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void testAddEventReview_WithoutAttendedReservation_ThrowsException() {
        when(eventRepository.existsById(200L)).thenReturn(true);
        when(eventReservationRepository.existsAttendedReservationByUserAndEvent(1L, 200L)).thenReturn(false);

        assertThrows(ReviewNotEligibleException.class,
                () -> reviewService.addEventReview(200L, 1L, testDto));

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void testUpdateMyReview_WithValidData_UpdatesReview() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        ReviewResponseDto result = reviewService.updateMyReview(1L, 1L, testDto);

        assertNotNull(result);
        assertTrue(result.getCanEdit());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void testUpdateMyReview_WithWrongUser_ThrowsException() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

        assertThrows(ReviewNotAllowedException.class,
                () -> reviewService.updateMyReview(1L, 999L, testDto));

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void testUpdateMyReview_WithHiddenReview_ThrowsException() {
        testReview.setStatus(ReviewStatus.HIDDEN);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

        assertThrows(ReviewNotAllowedException.class,
                () -> reviewService.updateMyReview(1L, 1L, testDto));

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void testUpdateMyReview_WithNonExistentReview_ThrowsException() {
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ReviewNotFoundException.class,
                () -> reviewService.updateMyReview(999L, 1L, testDto));
    }

    @Test
    void testDeleteMyReview_WithReviewOwner_DeletesReview() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

        reviewService.deleteMyReview(1L, 1L);

        verify(reviewRepository, times(1)).delete(testReview);
    }

    @Test
    void testDeleteMyReview_WithAdminUser_DeletesReview() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("ADMIN");

            reviewService.deleteMyReview(1L, 999L);

            verify(reviewRepository, times(1)).delete(testReview);
        }
    }

    @Test
    void testDeleteMyReview_WithWrongUser_ThrowsException() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("USER");

            assertThrows(ReviewNotAllowedException.class,
                    () -> reviewService.deleteMyReview(1L, 999L));

            verify(reviewRepository, never()).delete(testReview);
        }
    }

    @Test
    void testDeleteMyReview_WithNonExistentReview_ThrowsException() {
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ReviewNotFoundException.class,
                () -> reviewService.deleteMyReview(999L, 1L));
    }

    @Test
    void testListByHotel_WithValidHotel_ReturnsReviews() {
        when(hotelRepository.existsById(100L)).thenReturn(true);

        List<Review> reviews = List.of(testReview);
        Page<Review> page = new PageImpl<>(reviews);

        when(reviewRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        Pageable pageable = PageRequest.of(0, 10);
        PagedResponse<ReviewResponseDto> result = reviewService.listByHotel(100L, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(0, result.getPage());
        verify(reviewRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void testListByHotel_WithNonExistentHotel_ThrowsException() {
        when(hotelRepository.existsById(999L)).thenReturn(false);

        Pageable pageable = PageRequest.of(0, 10);
        assertThrows(HotelNotFoundException.class,
                () -> reviewService.listByHotel(999L, null, pageable));
    }

    @Test
    void testListByEvent_WithValidEvent_ReturnsReviews() {
        when(eventRepository.existsById(200L)).thenReturn(true);

        List<Review> reviews = List.of(testReview);
        Page<Review> page = new PageImpl<>(reviews);

        when(reviewRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        Pageable pageable = PageRequest.of(0, 10);
        PagedResponse<ReviewResponseDto> result = reviewService.listByEvent(200L, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(reviewRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void testListByEvent_WithNonExistentEvent_ThrowsException() {
        when(eventRepository.existsById(999L)).thenReturn(false);

        Pageable pageable = PageRequest.of(0, 10);
        assertThrows(EventNotFoundException.class,
                () -> reviewService.listByEvent(999L, null, pageable));
    }

    @Test
    void testListByHotel_WithNewestSort_AppliesSortCorrectly() {
        when(hotelRepository.existsById(100L)).thenReturn(true);

        List<Review> reviews = List.of(testReview);
        Page<Review> page = new PageImpl<>(reviews);

        when(reviewRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        Pageable pageable = PageRequest.of(0, 10);
        PagedResponse<ReviewResponseDto> result = reviewService.listByHotel(100L, ReviewSort.NEWEST, pageable);

        assertNotNull(result);
        verify(reviewRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void testSummary_WithHotel_ReturnsSummary() {
        when(hotelRepository.existsById(100L)).thenReturn(true);
        when(reviewRepository.countByTargetTypeAndTargetIdAndStatus(ReviewTargetType.HOTEL, 100L, ReviewStatus.PUBLISHED))
                .thenReturn(5L);
        doReturn(0L).when(reviewRepository).countByTargetTypeAndTargetIdAndStatusAndRating(ReviewTargetType.HOTEL, 100L, ReviewStatus.PUBLISHED, 1);
        doReturn(0L).when(reviewRepository).countByTargetTypeAndTargetIdAndStatusAndRating(ReviewTargetType.HOTEL, 100L, ReviewStatus.PUBLISHED, 2);
        doReturn(0L).when(reviewRepository).countByTargetTypeAndTargetIdAndStatusAndRating(ReviewTargetType.HOTEL, 100L, ReviewStatus.PUBLISHED, 3);
        doReturn(2L).when(reviewRepository).countByTargetTypeAndTargetIdAndStatusAndRating(ReviewTargetType.HOTEL, 100L, ReviewStatus.PUBLISHED, 4);
        doReturn(3L).when(reviewRepository).countByTargetTypeAndTargetIdAndStatusAndRating(ReviewTargetType.HOTEL, 100L, ReviewStatus.PUBLISHED, 5);

        ReviewSummaryResponseDto result = reviewService.summary(ReviewTargetType.HOTEL, 100L);

        assertNotNull(result);
        assertEquals(5L, result.getCount());
        assertEquals(4.6, result.getAvgRating(), 0.01);
    }

    @Test
    void testSummary_WithNonExistentHotel_ThrowsException() {
        when(hotelRepository.existsById(999L)).thenReturn(false);

        assertThrows(HotelNotFoundException.class,
                () -> reviewService.summary(ReviewTargetType.HOTEL, 999L));
    }

    @Test
    void testSummary_WithNonExistentEvent_ThrowsException() {
        when(eventRepository.existsById(999L)).thenReturn(false);

        assertThrows(EventNotFoundException.class,
                () -> reviewService.summary(ReviewTargetType.EVENT, 999L));
    }

    @Test
    void testSummary_WithZeroReviews_ReturnsZeroAverage() {
        when(hotelRepository.existsById(100L)).thenReturn(true);
        when(reviewRepository.countByTargetTypeAndTargetIdAndStatus(ReviewTargetType.HOTEL, 100L, ReviewStatus.PUBLISHED))
                .thenReturn(0L);

        doReturn(0L).when(reviewRepository).countByTargetTypeAndTargetIdAndStatusAndRating(ReviewTargetType.HOTEL, 100L, ReviewStatus.PUBLISHED, 1);
        doReturn(0L).when(reviewRepository).countByTargetTypeAndTargetIdAndStatusAndRating(ReviewTargetType.HOTEL, 100L, ReviewStatus.PUBLISHED, 2);
        doReturn(0L).when(reviewRepository).countByTargetTypeAndTargetIdAndStatusAndRating(ReviewTargetType.HOTEL, 100L, ReviewStatus.PUBLISHED, 3);
        doReturn(0L).when(reviewRepository).countByTargetTypeAndTargetIdAndStatusAndRating(ReviewTargetType.HOTEL, 100L, ReviewStatus.PUBLISHED, 4);
        doReturn(0L).when(reviewRepository).countByTargetTypeAndTargetIdAndStatusAndRating(ReviewTargetType.HOTEL, 100L, ReviewStatus.PUBLISHED, 5);

        ReviewSummaryResponseDto result = reviewService.summary(ReviewTargetType.HOTEL, 100L);

        assertNotNull(result);
        assertEquals(0L, result.getCount());
        assertEquals(0.0, result.getAvgRating());
    }

    @Test
    void testHide_WithPublishedReview_HidesReview() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        reviewService.hide(1L);

        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void testHide_WithAlreadyHiddenReview_ThrowsException() {
        testReview.setStatus(ReviewStatus.HIDDEN);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

        assertThrows(IllegalArgumentException.class, () -> reviewService.hide(1L));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void testPublish_WithHiddenReview_PublishesReview() {
        testReview.setStatus(ReviewStatus.HIDDEN);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        reviewService.publish(1L);

        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void testPublish_WithAlreadyPublishedReview_ThrowsException() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

        assertThrows(IllegalArgumentException.class, () -> reviewService.publish(1L));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void testReport_WithValidReason_ReportsReview() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        reviewService.report(1L, "Inappropriate content");

        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void testReport_WithNullReason_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> reviewService.report(1L, null));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void testReport_WithBlankReason_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> reviewService.report(1L, "   "));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void testAddPhotoToReview_WithReviewOwner_AddsPhoto() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("USER");

            reviewService.addPhotoToReview(1L, "https://example.com/photo.jpg", 1L);

            verify(reviewRepository, times(1)).save(any(Review.class));
        }
    }

    @Test
    void testAddPhotoToReview_WithAdminUser_AddsPhoto() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("ADMIN");

            reviewService.addPhotoToReview(1L, "https://example.com/photo.jpg", 999L);

            verify(reviewRepository, times(1)).save(any(Review.class));
        }
    }

    @Test
    void testAddPhotoToReview_WithWrongUser_ThrowsException() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("USER");

            assertThrows(ReviewNotAllowedException.class,
                    () -> reviewService.addPhotoToReview(1L, "https://example.com/photo.jpg", 999L));

            verify(reviewRepository, never()).save(any(Review.class));
        }
    }
}
