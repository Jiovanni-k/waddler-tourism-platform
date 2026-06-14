package org.example.rest.booking;

import jakarta.persistence.*;
import lombok.*;
import org.example.rest.cancellationpolicy.CancellationPolicyName;
import org.example.rest.room.Room;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "room")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    private Integer numberOfGuests;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private Double totalPrice;

    private LocalDateTime bookingDate;

    @Column(name = "check_in_date")
    private LocalDate checkInDate;

    @Column(name = "check_out_date")
    private LocalDate checkOutDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancellation_policy_name", length = 50)
    private CancellationPolicyName cancellationPolicyName;

    @Column(name = "cancellation_policy_description", columnDefinition = "TEXT")
    private String cancellationPolicyDescription;

    @Column(name = "cancellation_days_before_checkin")
    private Integer cancellationDaysBeforeCheckin;

    @Column(name = "cancellation_refund_percentage", precision = 5, scale = 2)
    private BigDecimal cancellationRefundPercentage;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}