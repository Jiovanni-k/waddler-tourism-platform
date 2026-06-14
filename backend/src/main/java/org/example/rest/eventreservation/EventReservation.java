package org.example.rest.eventreservation;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.example.rest.event.Event;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "event_reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"event"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EventReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "reservation_code", nullable = false, unique = true, length = 20)
    private String reservationCode;

    @NotNull(message = "Participants count is required")
    @Min(value = 1, message = "Participants count must be at least 1")
    @Max(value = 100, message = "Participants count cannot exceed 100")
    @Column(name = "participants_count", nullable = false)
    private Integer participantsCount = 1;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.00", message = "Total amount must be 0 or greater")
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    @Column(nullable = false, length = 3)
    private String currency = "USD";

    @DecimalMin(value = "0.00", message = "Discount percentage must be between 0 and 100")
    @DecimalMax(value = "100.00", message = "Discount percentage must be between 0 and 100")
    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", length = 20)
    private DifficultyLevel difficultyLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_restriction", length = 20)
    private AgeRestriction ageRestriction = AgeRestriction.ALL_AGES;

    @Column(name = "min_age")
    private Integer minAge;

    @Column(name = "meeting_point", length = 255)
    private String meetingPoint;

    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventReservationStatus status = EventReservationStatus.PENDING;

    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
}