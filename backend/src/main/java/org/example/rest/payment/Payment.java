package org.example.rest.payment;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.example.rest.booking.Booking;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "booking")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "Payment code is required")
    @Column(name = "payment_code", nullable = false, unique = true, length = 20)
    private String paymentCode;

    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.00", message = "Amount must be 0 or greater")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    @Column(nullable = false, length = 3)
    private String currency = "USD";

    @NotNull(message = "Payment method is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @NotNull(message = "Payment status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 30)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "payment_intent_id", length = 100)
    private String paymentIntentId;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "split_between_users", columnDefinition = "TEXT")
    private String splitBetweenUsers;

    @NotNull
    @Column(name = "fraud_detection_flag", nullable = false)
    private Boolean fraudDetectionFlag = false;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}