package org.example.rest.security.user;

public enum UserStatus {

    ACTIVE("Active"),
    SUSPENDED("Suspended"),
    PENDING_APPROVAL("Pending Approval");

    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}