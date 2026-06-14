package org.example.rest.event;

import java.util.Objects;

public final class EventMapper {

    private EventMapper() {}

    public static Event toEntity(EventRequestDto request) {
        if (Objects.isNull(request)) return null;
        Event event = new Event();
        applyChanges(request, event);
        return event;
    }

    public static void applyChanges(EventRequestDto request, Event event) {
        if (Objects.isNull(request) || Objects.isNull(event)) return;

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setCategory(request.getCategory());
        event.setTags(request.getTags());

        event.setStartDateTime(request.getStartDateTime());
        event.setEndDateTime(request.getEndDateTime());
        event.setLocationType(request.getLocationType());
        event.setAddress(request.getAddress());
        event.setCity(request.getCity());

        event.setPrice(request.getPrice());
        event.setCurrency(Objects.nonNull(request.getCurrency()) ? request.getCurrency() : "USD");
        event.setCapacityTotal(request.getCapacityTotal());
        event.setMaxPerUser(Objects.nonNull(request.getMaxPerUser()) ? request.getMaxPerUser() : 1);
        event.setBookingCutoffMinutes(Objects.nonNull(request.getBookingCutoffMinutes()) ? request.getBookingCutoffMinutes() : 60);

        event.setPhotos(request.getPhotos());
        event.setBannerImageUrl(request.getBannerImageUrl());

        event.setRefundEnabled(Objects.nonNull(request.getRefundEnabled()) ? request.getRefundEnabled() : false);
        event.setRefundPercent(request.getRefundPercent());
        event.setRequiresApproval(Objects.nonNull(request.getRequiresApproval()) ? request.getRequiresApproval() : false);
    }

    public static EventResponseDto toResponse(Event event) {
        if (Objects.isNull(event)) return null;

        EventResponseDto response = new EventResponseDto();

        response.setId(event.getId());
        response.setHotelId(Objects.nonNull(event.getHotel()) ? event.getHotel().getId() : null);

        response.setTitle(event.getTitle());
        response.setDescription(event.getDescription());
        response.setCategory(event.getCategory());
        response.setTags(event.getTags());

        response.setStartDateTime(event.getStartDateTime());
        response.setEndDateTime(event.getEndDateTime());
        response.setLocationType(event.getLocationType());
        response.setAddress(event.getAddress());
        response.setCity(event.getCity());

        response.setPrice(event.getPrice());
        response.setCurrency(event.getCurrency());
        response.setCapacityTotal(event.getCapacityTotal());
        response.setMaxPerUser(event.getMaxPerUser());
        response.setBookingCutoffMinutes(event.getBookingCutoffMinutes());

        response.setPhotos(event.getPhotos());
        response.setBannerImageUrl(event.getBannerImageUrl());

        response.setStatus(event.getStatus());
        response.setRefundEnabled(event.getRefundEnabled());
        response.setRefundPercent(event.getRefundPercent());
        response.setRequiresApproval(event.getRequiresApproval());

        response.setCreatedAt(event.getCreatedAt());
        response.setUpdatedAt(event.getUpdatedAt());

        response.setReservationsCount(
                Objects.nonNull(event.getEventReservations())
                        ? event.getEventReservations().size()
                        : 0);

        return response;
    }

    public static EventResponseDto toResponseWithRatings(Event event, Double avgRating, Long reviewsCount) {
        EventResponseDto response = toResponse(event);
        if (Objects.isNull(response)) return null;
        response.setAvgRating(avgRating);
        response.setReviewsCount(reviewsCount);
        return response;
    }
}