package org.example.rest.contact;

public enum ContactCategory {
    GENERAL_INQUIRY("General Inquiry"),
    BOOKING_SUPPORT("Booking Support"),
    TECHNICAL_ISSUE("Technical Issue"),
    PARTNERSHIP("Partnership"),
    FEEDBACK("Feedback"),
    OTHER("Other");

    private final String displayName;

    ContactCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}