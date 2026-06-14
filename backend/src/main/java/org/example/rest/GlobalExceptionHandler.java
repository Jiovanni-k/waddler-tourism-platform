package org.example.rest;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.example.rest.amenity.AmenityNotFoundException;
import org.example.rest.amenity.DuplicateAmenityException;
import org.example.rest.booking.*;
import org.example.rest.cancellationpolicy.CancellationPolicyNotFoundException;
import org.example.rest.cancellationpolicy.CancellationPolicyNotBelongToHotelException;
import org.example.rest.cancellationpolicy.DuplicateCancellationPolicyException;
import org.example.rest.contact.ContactNotFoundException;
import org.example.rest.event.EventForbiddenActionException;
import org.example.rest.event.EventNotFoundException;
import org.example.rest.eventreservation.EventReservationForbiddenActionException;
import org.example.rest.eventreservation.EventReservationNotFoundException;
import org.example.rest.hotel.HotelNotFoundException;
import org.example.rest.inventory.InventoryNotFoundException;
import org.example.rest.pricingrule.DuplicatePricingRuleException;
import org.example.rest.pricingrule.PricingRuleNotFoundException;
import org.example.rest.room.RoomNotFoundException;
import org.example.rest.room.RoomNotBelongToHotelException;
import org.example.rest.room.DuplicateRoomException;
import org.example.rest.room.RoomHasActiveBookingsException;
import org.example.rest.payment.PaymentNotFoundException;
import org.example.rest.payment.PaymentForbiddenActionException;
import org.example.rest.payment.DuplicatePaymentException;
import org.example.rest.payment.refund.RefundNotFoundException;
import org.example.rest.payment.refund.RefundNotAllowedException;
import org.example.rest.review.AlreadyReviewedException;
import org.example.rest.review.ReviewNotAllowedException;
import org.example.rest.review.ReviewNotEligibleException;
import org.example.rest.review.ReviewNotFoundException;
import org.example.rest.security.AuthException;
import org.example.rest.security.user.UserNotFoundException;
import org.example.rest.tablereservation.TableReservationForbiddenActionException;
import org.example.rest.tablereservation.TableReservationNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({
            EventNotFoundException.class,
            EventReservationNotFoundException.class,
            ReviewNotFoundException.class,
            BookingNotFoundException.class,
            HotelNotFoundException.class,
            AmenityNotFoundException.class,
            CancellationPolicyNotFoundException.class,
            InventoryNotFoundException.class,
            TableReservationNotFoundException.class,
            ContactNotFoundException.class,
            PaymentNotFoundException.class,
            RefundNotFoundException.class,
            RoomNotFoundException.class,
            UserNotFoundException.class,
            PricingRuleNotFoundException.class
    })
    public ResponseEntity<ApiError> handleNotFound(RuntimeException ex, HttpServletRequest request) {
        logger.warn("Not found at {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ErrorMessages.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler({
            EventForbiddenActionException.class,
            EventReservationForbiddenActionException.class,
            BookingForbiddenActionException.class,
            TableReservationForbiddenActionException.class,
            CancellationPolicyNotBelongToHotelException.class,
            PaymentForbiddenActionException.class,
            RefundNotAllowedException.class,
            RoomNotBelongToHotelException.class,
            RoomHasActiveBookingsException.class
    })
    public ResponseEntity<ApiError> handleForbiddenAction(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ErrorMessages.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(ReviewNotAllowedException.class)
    public ResponseEntity<ApiError> handleReviewNotAllowed(ReviewNotAllowedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, ErrorMessages.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(ReviewNotEligibleException.class)
    public ResponseEntity<ApiError> handleNotEligible(ReviewNotEligibleException ex, HttpServletRequest request) {
        logger.warn("Review eligibility denied at {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.FORBIDDEN, ErrorMessages.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler({
            AlreadyReviewedException.class,
            DuplicateAmenityException.class,
            DuplicateCancellationPolicyException.class,
            DuplicatePaymentException.class,
            DuplicateRoomException.class,
            BookingConflictException.class,
            DuplicatePricingRuleException.class
    })
    public ResponseEntity<ApiError> handleConflict(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ErrorMessages.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class,
            InvalidPriceRangeException.class,
            AuthException.class
    })
    public ResponseEntity<ApiError> handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ErrorMessages.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<Map<String, String>> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> Map.of(
                        "field",   fe.getField(),
                        "message", fe.getDefaultMessage()
                ))
                .toList();

        Map<String, Object> body = Map.of(
                "timestamp", Instant.now().toString(),
                "status",    400,
                "error",     ErrorMessages.BAD_REQUEST,
                "path",      request.getRequestURI(),
                "errors",    errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        logger.warn("Malformed request at {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ErrorMessages.BAD_REQUEST,
                "Invalid request body. Please check your field values and try again.", request);
    }

    @ExceptionHandler(ConversionFailedException.class)
    public ResponseEntity<ApiError> handleConversionFailed(
            ConversionFailedException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.BAD_REQUEST, ErrorMessages.BAD_REQUEST,
                "Invalid value for request parameter or path variable", request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResourceFound(
            NoResourceFoundException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.BAD_REQUEST, ErrorMessages.BAD_REQUEST,
                "Invalid request path", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
        logger.error("Internal server error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessages.INTERNAL_SERVER_ERROR,
                ErrorMessages.SOMETHING_WENT_WRONG, request);
    }

    @ExceptionHandler({AccessDeniedException.class, BookingAccessDeniedException.class})
    public ResponseEntity<ApiError> handleAccessDenied(Exception ex, HttpServletRequest request) {
        return build(
                HttpStatus.FORBIDDEN,
                "Access Denied",
                ex.getMessage() != null ? ex.getMessage() : "You do not have permission to perform this action",
                request
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        String parameterName = ex.getName();
        String message = "Invalid value for parameter '" + parameterName + "'";

        if ("page".equals(parameterName) || "size".equals(parameterName)) {
            message = "Parameters 'page' and 'size' must be whole numbers";
        } else if ("sortBy".equals(parameterName)) {
            message = "Invalid sortBy value";
        } else if ("sortDir".equals(parameterName)) {
            message = "Invalid sortDir value";
        } else if ("minStars".equals(parameterName)) {
            message = "Parameter 'minStars' must be a whole number";
        } else if ("minRating".equals(parameterName)) {
            message = "Parameter 'minRating' must be a valid number";
        } else if ("q".equals(parameterName)) {
            message = "Parameter 'q' must be a valid string";
        }

        return build(HttpStatus.BAD_REQUEST, ErrorMessages.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(org.springframework.web.multipart.support.MissingServletRequestPartException.class)
    public ResponseEntity<ApiError> handleMissingPart(
            org.springframework.web.multipart.support.MissingServletRequestPartException ex,
            HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ErrorMessages.BAD_REQUEST, "File is empty", request);
    }

    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParam(
            org.springframework.web.bind.MissingServletRequestParameterException ex,
            HttpServletRequest request) {
        String message = "Required parameter '" + ex.getParameterName() + "' is missing";
        return build(HttpStatus.BAD_REQUEST, ErrorMessages.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(org.springframework.web.multipart.MultipartException.class)
    public ResponseEntity<ApiError> handleMultipart(
            org.springframework.web.multipart.MultipartException ex,
            HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ErrorMessages.BAD_REQUEST, "File is empty", request);
    }

    @ExceptionHandler({
            org.springframework.beans.TypeMismatchException.class,
            NumberFormatException.class
    })
    public ResponseEntity<ApiError> handleTypeProblems(
            Exception ex,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.BAD_REQUEST,
                ErrorMessages.BAD_REQUEST,
                "Invalid value for request parameter or path variable",
                request
        );
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String error, String message, HttpServletRequest request) {
        ApiError body = new ApiError(
                Instant.now().toString(),
                status.value(),
                error,
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }
}