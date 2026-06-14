package org.example.rest.security.user;

import java.util.Set;

public enum UserRole {

    USER("User") {
        @Override
        public Set<UserAuthority> getAuthorities() {
            return Set.of(
                    UserAuthority.VIEW_HOTEL,
                    UserAuthority.VIEW_EVENT,
                    UserAuthority.VIEW_REVIEW,
                    UserAuthority.VIEW_CANCELLATION_POLICY,
                    UserAuthority.VIEW_ROOM,
                    UserAuthority.CREATE_BOOKING,
                    UserAuthority.VIEW_BOOKING,
                    UserAuthority.UPDATE_BOOKING,
                    UserAuthority.CANCEL_BOOKING,
                    UserAuthority.REQUEST_BOOKING_CANCELLATION,
                    UserAuthority.CREATE_REVIEW,
                    UserAuthority.UPDATE_REVIEW,
                    UserAuthority.DELETE_REVIEW,
                    UserAuthority.VIEW_EVENT_RESERVATION,
                    UserAuthority.CREATE_EVENT_RESERVATION,
                    UserAuthority.CANCEL_EVENT_RESERVATION,
                    UserAuthority.CREATE_PAYMENT,
                    UserAuthority.VIEW_PAYMENT,
                    UserAuthority.CREATE_REFUND,
                    UserAuthority.VIEW_REFUND,
                    UserAuthority.VIEW_CONTACT,
                    UserAuthority.VIEW_LOYALTY,
                    UserAuthority.VIEW_AMENITY
            );
        }
    },

    HOTEL_MANAGER("Hotel Manager") {
        @Override
        public Set<UserAuthority> getAuthorities() {
            return Set.of(
                    UserAuthority.VIEW_HOTEL,
                    UserAuthority.CREATE_HOTEL,
                    UserAuthority.UPDATE_HOTEL,
                    UserAuthority.VIEW_CANCELLATION_POLICY,
                    UserAuthority.VIEW_ROOM,
                    UserAuthority.CREATE_ROOM,
                    UserAuthority.UPDATE_ROOM,
                    UserAuthority.DELETE_HOTEL,
                    UserAuthority.CREATE_CANCELLATION_POLICY,
                    UserAuthority.UPDATE_CANCELLATION_POLICY,
                    UserAuthority.DELETE_CANCELLATION_POLICY,
                    UserAuthority.VIEW_EVENT,
                    UserAuthority.CREATE_EVENT,
                    UserAuthority.UPDATE_EVENT,
                    UserAuthority.DELETE_EVENT,
                    UserAuthority.VIEW_BOOKING,
                    UserAuthority.CONFIRM_BOOKING,
                    UserAuthority.UPDATE_BOOKING,
                    UserAuthority.CANCEL_BOOKING,
                    UserAuthority.APPROVE_BOOKING_CANCELLATION,
                    UserAuthority.MANAGE_BOOKING_POLICY,
                    UserAuthority.DENY_BOOKING_CANCELLATION,
                    UserAuthority.VIEW_REVIEW,
                    UserAuthority.DELETE_REVIEW,
                    UserAuthority.VIEW_EVENT_RESERVATION,
                    UserAuthority.CONFIRM_EVENT_RESERVATION,
                    UserAuthority.CANCEL_EVENT_RESERVATION,
                    UserAuthority.VIEW_PAYMENT,
                    UserAuthority.MANAGE_PAYMENT,
                    UserAuthority.VIEW_REFUND,
                    UserAuthority.VIEW_CONTACT,
                    UserAuthority.VIEW_AMENITY,
                    UserAuthority.CREATE_AMENITY,
                    UserAuthority.UPDATE_AMENITY,
                    UserAuthority.DELETE_AMENITY,
                    UserAuthority.ATTEND_EVENT_RESERVATION,
                    UserAuthority.COMPLETE_EVENT_RESERVATION,
                    UserAuthority.NO_SHOW_EVENT_RESERVATION,
                    UserAuthority.UPDATE_EVENT_RESERVATION
            );
        }
    },

    ADMIN("Admin") {
        @Override
        public Set<UserAuthority> getAuthorities() {
            return Set.of(UserAuthority.values());
        }
    };

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public abstract Set<UserAuthority> getAuthorities();
}