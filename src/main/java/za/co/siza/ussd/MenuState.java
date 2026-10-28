package za.co.siza.ussd;

/**
 * The different screens a user can see during a USSD session.
 * 
 * Each state represents a specific point in the flow. The MenuFlow class 
 * uses these to figure out where the user is allowed to go next.
 */
public enum MenuState {
    
    WELCOME,          // The first screen. Main menu options (help, location, check-in).
    DANGER_CHECK,     // Asking if they are in immediate danger.
    PANIC_SENT,       // The panic alert has been triggered and sent.
    ASK_LOCATION,     // Asking if they want to type a location or say they don't know.
    LOCATION_TEXT,    // Waiting for the user to type a street name or landmark.
    LOCATION_UNKNOWN, // The user said they don't know where they are.
    REPORT_CONFIRMED, // The location report has been sent.
    CHECKIN_MENU,     // The safety check-in menu (Are you okay?).
    CHECKIN_OK,       // The user confirmed they are okay.
    GOODBYE,          // A friendly exit message before closing the session.
    END               // The final state. The session is over.
}
