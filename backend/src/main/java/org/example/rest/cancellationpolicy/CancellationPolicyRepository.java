package org.example.rest.cancellationpolicy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CancellationPolicyRepository extends JpaRepository<CancellationPolicy, Long>,
        JpaSpecificationExecutor<CancellationPolicy> {

    List<CancellationPolicy> findByHotel_Id(Long hotelId);

    boolean existsByIdAndHotel_Id(Long id, Long hotelId);

    Optional<CancellationPolicy> findByHotel_IdAndName(Long hotelId, CancellationPolicyName name);
}