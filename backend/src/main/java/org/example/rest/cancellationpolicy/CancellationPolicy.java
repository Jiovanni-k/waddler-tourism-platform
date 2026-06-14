package org.example.rest.cancellationpolicy;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.example.rest.hotel.Hotel;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "cancellation_policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "hotel")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CancellationPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @NotNull(message = "Policy name is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CancellationPolicyName name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Days before check-in is required")
    @Min(value = 0, message = "Days before check-in must be 0 or greater")
    @Column(name = "days_before_checkin", nullable = false)
    private Integer daysBeforeCheckin;

    @NotNull(message = "Refund percentage is required")
    @DecimalMin(value = "0.00", message = "Refund percentage must be between 0 and 100")
    @DecimalMax(value = "100.00", message = "Refund percentage must be between 0 and 100")
    @Column(name = "refund_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal refundPercentage;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}