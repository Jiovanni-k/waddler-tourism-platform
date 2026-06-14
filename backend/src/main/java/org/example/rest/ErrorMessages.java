package org.example.rest;

public final class ErrorMessages {

    private ErrorMessages() {}

    public static final String NOT_FOUND             = "Not Found";
    public static final String FORBIDDEN             = "Forbidden";
    public static final String BAD_REQUEST           = "Bad Request";
    public static final String CONFLICT              = "Conflict";
    public static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
    public static final String SOMETHING_WENT_WRONG  = "Something went wrong. Please try again later.";

    public static final String MALFORMED_JSON =
            "Malformed JSON or invalid field value. " +
                    "Ensure dates are in yyyy-MM-dd format, " +
                    "gender is one of: MALE, FEMALE, " +
                    "role is one of: USER, HOTEL_MANAGER, " +
                    "and maritalStatus is one of: SINGLE, MARRIED, DIVORCED, WIDOWED.";

    public static String eventNotFound(Long id)            { return "Event not found with id: " + id; }
    public static String eventReservationNotFound(Long id) { return "Event reservation not found with id: " + id; }
    public static String reviewNotFound(Long id)           { return "Review not found: " + id; }
    public static final String ALREADY_REVIEWED            = "You already reviewed this target.";

    public static String amenityNotFound(Long id)          { return "Amenity not found with id: " + id; }
    public static final String DUPLICATE_AMENITY           = "An amenity with this name already exists.";

    public static String hotelNotFound(Long id)            { return "Hotel not found with id: " + id; }
    public static String roomNotFound(Long id)             { return "Room not found with id: " + id; }

    public static final String USER_NOT_FOUND              = "User not found.";
    public static String userNotFoundByEmail(String email) { return "User not found with email: " + email; }
    public static String userNotFoundById(Long id)         { return "User not found with id: " + id; }
}