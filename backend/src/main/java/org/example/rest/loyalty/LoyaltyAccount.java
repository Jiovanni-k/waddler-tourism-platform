package org.example.rest.loyalty;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "loyalty_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class LoyaltyAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    @Builder.Default
    private Long pointsBalance = 0L;

    @Column(name = "lifetime_points", nullable = false)
    @Builder.Default
    private Long lifetimePoints = 0L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private LoyaltyTier tier = LoyaltyTier.NONE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private LoyaltyStatus status = LoyaltyStatus.ACTIVE;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void addPoints(long points) {
        this.pointsBalance += points;
        this.lifetimePoints += points;
        this.tier = LoyaltyTier.fromLifetimePoints(this.lifetimePoints);
    }

    public void redeemPoints(long points) {
        if (points > this.pointsBalance)
            throw new IllegalArgumentException("Insufficient points balance");
        this.pointsBalance -= points;
    }
}