package org.example.rest.loyalty;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "loyalty_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class LoyaltyTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long points;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private LoyaltyTransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LoyaltyRewardType rewardType;

    @Column(name = "related_id")
    private Long relatedId;

    @Column(length = 255)
    private String note;

    @Column(name = "balance_after", nullable = false)
    private Long balanceAfter;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}