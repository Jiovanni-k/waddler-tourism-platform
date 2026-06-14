package org.example.rest.security.user;

public enum UserGender {

    MALE("Male"),
    FEMALE("Female");

    private final String displayName;

    UserGender(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}