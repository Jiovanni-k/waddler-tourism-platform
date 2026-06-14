package org.example.rest.room;

public enum RoomType {

    SINGLE("Single"),
    DOUBLE("Double"),
    TWIN("Twin"),
    TRIPLE("Triple"),
    SUITE("Suite"),
    DELUXE("Deluxe"),
    FAMILY("Family"),
    STUDIO("Studio"),
    PENTHOUSE("Penthouse"),
    ACCESSIBLE("Accessible");

    private final String displayName;

    RoomType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}