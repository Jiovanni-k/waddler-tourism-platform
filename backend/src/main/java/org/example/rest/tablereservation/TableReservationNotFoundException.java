package org.example.rest.tablereservation;

public class TableReservationNotFoundException extends RuntimeException {
    public TableReservationNotFoundException(Long id) {
        super("Table reservation with id " + id + " not found");
    }
}