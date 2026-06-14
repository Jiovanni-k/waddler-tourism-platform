package org.example.rest.amenity;

public enum AmenityCategory {
    WIFI("WIFI"),
    PARKING("PARKING"),
    POOL("POOL"),
    GYM("GYM"),
    RESTAURANT("RESTAURANT"),
    BAR("BAR"),
    SPA("SPA"),
    CONFERENCE_ROOM("CONFERENCE_ROOM"),
    LAUNDRY("LAUNDRY"),
    ROOM_SERVICE("ROOM_SERVICE"),
    CONCIERGE("CONCIERGE"),
    BUSINESS_CENTER("BUSINESS_CENTER"),
    PETS_ALLOWED("PETS_ALLOWED"),
    ACCESSIBLE("ACCESSIBLE"),
    OTHER("OTHER");


    private final String displayName;

    AmenityCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}