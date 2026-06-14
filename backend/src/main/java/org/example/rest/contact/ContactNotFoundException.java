package org.example.rest.contact;

public class ContactNotFoundException extends RuntimeException {
    public ContactNotFoundException(Long id) {
        super("Contact request with id " + id + " not found");
    }
}