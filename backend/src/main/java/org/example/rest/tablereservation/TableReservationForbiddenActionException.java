package org.example.rest.tablereservation;

public class TableReservationForbiddenActionException extends RuntimeException {
    public TableReservationForbiddenActionException(String message) {
        super(message);
    }
}