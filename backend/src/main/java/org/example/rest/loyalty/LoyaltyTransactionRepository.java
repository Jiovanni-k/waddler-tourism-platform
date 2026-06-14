package org.example.rest.loyalty;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Long> {

    Page<LoyaltyTransaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    boolean existsByUserIdAndRelatedIdAndRewardType(Long userId, Long relatedId, LoyaltyRewardType rewardType);
}