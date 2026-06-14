package org.example.rest.payment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {

    Optional<Payment> findByPaymentCode(String paymentCode);

    Page<Payment> findByUserId(Long userId, Pageable pageable);

    List<Payment> findByBooking_Id(Long bookingId);

    List<Payment> findByPaymentStatus(PaymentStatus status);

    boolean existsByBooking_IdAndPaymentStatusIn(Long bookingId, List<PaymentStatus> statuses);
}