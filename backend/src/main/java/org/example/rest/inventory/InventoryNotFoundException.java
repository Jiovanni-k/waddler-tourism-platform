package org.example.rest.inventory;

public class InventoryNotFoundException extends RuntimeException {

    public InventoryNotFoundException(Long id) {
        super("Inventory not found with id: " + id);
    }

    public InventoryNotFoundException(Long roomId, java.time.LocalDate date) {
        super("Inventory not found for roomId: " + roomId + " on date: " + date);
    }
}