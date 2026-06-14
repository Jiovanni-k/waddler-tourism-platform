package org.example.rest.hotel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface HotelRepository extends JpaRepository<Hotel, Long>, JpaSpecificationExecutor<Hotel> {

    Page<Hotel> findByStatus(HotelStatus status, Pageable pageable);

    List<Hotel> findAllByStatus(HotelStatus status);

    Page<Hotel> findByCityIgnoreCaseAndStatus(String city, HotelStatus status, Pageable pageable);

    List<Hotel> findByManagerId(Long managerId);

    Optional<Hotel> findByIdAndManagerId(Long id, Long managerId);

    @Query("SELECT h FROM Hotel h WHERE h.status = 'ACTIVE' AND " +
            "(LOWER(h.name) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(h.city) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<Hotel> searchByNameOrCity(@Param("q") String query, Pageable pageable);

    @Query("SELECT h FROM Hotel h WHERE h.status = 'ACTIVE' " +
            "AND (:city IS NULL OR LOWER(h.city) = LOWER(:city)) " +
            "AND (:minStars IS NULL OR h.starRating >= :minStars) " +
            "AND (:minRating IS NULL OR h.averageGuestRating >= :minRating)")
    Page<Hotel> findWithFilters(
            @Param("city") String city,
            @Param("minStars") Integer minStars,
            @Param("minRating") BigDecimal minRating,
            Pageable pageable);
}