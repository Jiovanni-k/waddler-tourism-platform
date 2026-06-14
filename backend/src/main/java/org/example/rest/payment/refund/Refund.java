package org.example.rest.payment.refund;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.example.rest.payment.Payment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "refunds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "payment")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull(message = "Payment is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @NotBlank(message = "Refund code is required")
    @Column(name = "refund_code", nullable = false, unique = true, length = 20)
    private String refundCode;

    @NotNull(message = "Amount is required")
    @Min(value = 0, message = "Amount must be 0 or greater")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    @Column(nullable = false, length = 3)
    private String currency = "USD";

    @NotNull(message = "Refund percentage is required")
    @Min(value = 0, message = "Refund percentage must be between 0 and 100")
    @Max(value = 100, message = "Refund percentage must be between 0 and 100")
    @Column(name = "refund_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal refundPercentage;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RefundStatus status = RefundStatus.PENDING;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Version
    private Long version;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Long getPaymentId() {
        return payment != null ? payment.getId() : null;
    }
}