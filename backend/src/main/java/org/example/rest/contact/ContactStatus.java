package org.example.rest.contact;

public enum ContactStatus {
    NEW("New"),
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved"),
    CLOSED("Closed");

    private final String displayName;

    ContactStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}