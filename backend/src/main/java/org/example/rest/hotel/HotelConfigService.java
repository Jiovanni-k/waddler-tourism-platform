package org.example.rest.hotel;

import java.util.Set;

public class HotelConfigService {

    private static HotelConfigService instance;

    private final Set<String> allowedSortFields = Set.of(
            "id", "name", "city", "region",
            "starRating", "averageGuestRating", "createdAt", "updatedAt"
    );

    private final int maxGalleryImages = 20;
    private final int maxAmenitiesPerHotel = 50;
    private HotelConfigService() {}
    public static HotelConfigService getInstance() {
        if (instance == null) {
            instance = new HotelConfigService();
        }
        return instance;
    }
    public Set<String> getAllowedSortFields() { return allowedSortFields; }
    public int getMaxGalleryImages()          { return maxGalleryImages; }
    public int getMaxAmenitiesPerHotel()      { return maxAmenitiesPerHotel; }
}