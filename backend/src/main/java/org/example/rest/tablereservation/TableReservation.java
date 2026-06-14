package org.example.rest.tablereservation;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.example.rest.hotel.Hotel;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "table_reservations")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "hotel")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TableReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "reservation_code", nullable = false, unique = true, length = 20)
    private String reservationCode;

    @NotNull(message = "Guest count is required")
    @Min(value = 1, message = "Guest count must be at least 1")
    @Column(name = "guest_count", nullable = false)
    private Integer guestCount;

    @NotNull(message = "Reservation date and time is required")
    @Column(name = "reservation_date_time", nullable = false)
    private LocalDateTime reservationDateTime;

    @NotNull(message = "Duration is required")
    @Min(value = 30, message = "Duration must be at least 30 minutes")
    @Max(value = 300, message = "Duration cannot exceed 5 hours")
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes = 120;

    @Enumerated(EnumType.STRING)
    @Column(name = "special_occasion", length = 100)
    private SpecialOccasion specialOccasion;

    @Column(name = "table_number", length = 20)
    private String tableNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "table_type", length = 20)
    private TableType tableType;

    @Column(name = "pre_order_items", columnDefinition = "TEXT")
    private String preOrderItems;

    @Column(name = "dietary_restrictions", length = 255)
    private String dietaryRestrictions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TableReservationStatus status = TableReservationStatus.PENDING;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;
}