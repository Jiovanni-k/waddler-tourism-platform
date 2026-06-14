package org.example.rest.review;

public final class ReviewMapper {

    private ReviewMapper() {}

    public static Review toEntity(ReviewRequestDto dto) {
        if (dto == null) return null;
        Review r = new Review();
        apply(dto, r);
        return r;
    }

    public static void apply(ReviewRequestDto dto, Review r) {
        if (dto == null || r == null) return;

        r.setRating(dto.getRating());
        r.setComment(dto.getComment());
        r.setPhotos(dto.getPhotos());
        r.setVisitDate(dto.getVisitDate());
    }

    public static ReviewResponseDto toDto(Review r) {
        if (r == null) return null;

        ReviewResponseDto dto = new ReviewResponseDto();

        dto.setId(r.getId());
        dto.setTargetType(r.getTargetType());
        dto.setTargetId(r.getTargetId());

        dto.setRating(r.getRating());
        dto.setComment(r.getComment());
        dto.setPhotos(r.getPhotos());

        dto.setStatus(r.getStatus());
        dto.setVisitDate(r.getVisitDate());
        dto.setReplyFromManager(r.getReplyFromManager());
        dto.setReplyAt(r.getReplyAt());

        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());

        if (r.getReviewer() != null) {
            dto.setReviewerName(r.getReviewer().getFirstName()
                    + " " + r.getReviewer().getLastName());
        }

        return dto;
    }
}