package za.co.siza.domain;

/**
 * The different reasons someone might create an alert.
 */
public enum AlertType {
    
    PANIC,           // The user is in immediate danger and needs help right now.
    LOCATION_REPORT, // The user is sharing where they are, but isn't necessarily in danger.
    CHECKIN          // The user is letting contacts know they are safe.
}
