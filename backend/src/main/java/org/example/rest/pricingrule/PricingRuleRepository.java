package org.example.rest.pricingrule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {

    List<PricingRule> findByRoom_Id(Long roomId);

    List<PricingRule> findByRoom_IdAndActiveTrue(Long roomId);

    @Query("SELECT r FROM PricingRule r " +
            "WHERE r.room.id = :roomId " +
            "AND r.active = true " +
            "AND r.startDate <= :checkOut " +
            "AND r.endDate >= :checkIn " +
            "ORDER BY r.priority DESC")
    List<PricingRule> findActiveRulesForDateRange(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);

    boolean existsByRoom_IdAndNameAndIdNot(Long roomId, String name, Long id);

    boolean existsByRoom_IdAndName(Long roomId, String name);
}