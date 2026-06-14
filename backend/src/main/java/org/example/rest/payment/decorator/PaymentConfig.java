package org.example.rest.payment.decorator;

import org.example.rest.booking.BookingRepository;
import org.example.rest.hotel.HotelRepository;
import org.example.rest.payment.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

// Decorator Pattern — Wiring the chain.
// This is the only place in the entire codebase that knows about
// all the decorators. The chain runs in this order:
//
//   Request → FraudCheckDecorator
//           → DuplicatePaymentGuardDecorator
//           → AmountValidationDecorator
//           → PaymentServiceImpl (core)
//
// To add a new concern (e.g. rate limiting), create a new decorator
// class and wrap it here — zero changes to any existing class.
@Configuration
public class PaymentConfig {

    @Bean
    @Primary
    public PaymentService paymentService(
            PaymentServiceImpl core,
            PaymentRepository paymentRepository,
            BookingRepository bookingRepository,
            HotelRepository hotelRepository) {

        PaymentService withAmountValidation =
                new AmountValidationDecorator(core, bookingRepository);

        PaymentService withDuplicateGuard =
                new DuplicatePaymentGuardDecorator(withAmountValidation, paymentRepository);

        PaymentService withFraudCheck =
                new FraudCheckDecorator(withDuplicateGuard, paymentRepository, hotelRepository);

        return withFraudCheck;
    }
}