package org.example.rest.payment.refund;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long>, JpaSpecificationExecutor<Refund> {

    Optional<Refund> findByRefundCode(String refundCode);

    @org.springframework.data.jpa.repository.Query("SELECT r FROM Refund r WHERE r.payment.id = :paymentId")
    List<Refund> findByPaymentId(@org.springframework.data.repository.query.Param("paymentId") Long paymentId);

    List<Refund> findByStatus(RefundStatus status);
}