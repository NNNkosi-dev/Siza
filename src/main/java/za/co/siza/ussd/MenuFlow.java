package za.co.siza.ussd;

/**
 * The "brain" of the USSD app.
 * 
 * This class takes the current screen the user is on, looks at what 
 * they typed, and decides which screen to show them next. 
 * It doesn't do any networking or printing; it just handles the logic 
 * of moving through the menus.
 */
public final class MenuFlow {

    /**
     * Figures out the next screen based on where we are and what the user typed.
     */
    public MenuState next(MenuState current, String input) {
        // If the input is null (like a timeout), treat it as an empty string.
        // Also, trim any accidental spaces so " 1 " becomes "1".
        if (input == null) input = "";
        input = input.trim();

        return switch (current) {
            // The first screen. They can choose help, share location, or check in.
            case WELCOME -> switch (input) {
                case "1" -> MenuState.DANGER_CHECK;
                case "2" -> MenuState.ASK_LOCATION;
                case "3" -> MenuState.CHECKIN_MENU;
                default  -> MenuState.WELCOME; // If they type 9, just show the menu again.
            };
            
            // They chose "I need help now". Let's ask if they are in immediate danger.
            case DANGER_CHECK -> switch (input) {
                case "1" -> MenuState.PANIC_SENT; // Immediate danger -> Send panic alert.
                case "2" -> MenuState.ASK_LOCATION; // Feeling unsafe -> Let them share location.
                case "3" -> MenuState.ASK_LOCATION; // Not sure -> Treat it as sharing location.
                default  -> MenuState.DANGER_CHECK;
            };
            
            // We asked where they are. 
            case ASK_LOCATION -> switch (input) {
                case "1" -> MenuState.LOCATION_TEXT;    // They want to type a street name.
                case "2" -> MenuState.LOCATION_UNKNOWN; // They have no idea where they are.
                default  -> MenuState.ASK_LOCATION;
            };
            
            // The user just typed a street name or landmark. 
            // We don't need to check what they typed; we just move forward.
            case LOCATION_TEXT -> MenuState.REPORT_CONFIRMED;
            
            // They said they don't know where they are.
            case LOCATION_UNKNOWN -> MenuState.REPORT_CONFIRMED;
            
            // They are doing a safety check-in.
            case CHECKIN_MENU -> switch (input) {
                case "1" -> MenuState.CHECKIN_OK;       // They are okay.
                case "2" -> MenuState.ASK_LOCATION;     // Actually, they need help.
                default  -> MenuState.CHECKIN_MENU;
            };
            
            // They confirmed they are okay.
            case CHECKIN_OK -> MenuState.GOODBYE;
            
            // These are all the "end of the line" screens. 
            // Once they reach one of these, the next step (if they press anything) 
            // is to end the session.
            case PANIC_SENT, REPORT_CONFIRMED, GOODBYE, END -> MenuState.END;
        };
    }

    /**
     * Sometimes the user needs to type a real sentence (like a street name), 
     * instead of just pressing '1' or '2'. The server needs to know this 
     * so it doesn't try to process "Corner Main Rd" as a menu option.
     */
    public boolean expectsFreeText(MenuState state) {
        return state == MenuState.LOCATION_TEXT;
    }

    /**
     * Tells the server if the session is finished. 
     * If this returns true, the server knows it's time to create an Alert, 
     * send the SMS, and delete the session.
     */
    public boolean isTerminal(MenuState state) {
        return state == MenuState.PANIC_SENT
            || state == MenuState.REPORT_CONFIRMED
            || state == MenuState.LOCATION_UNKNOWN
            || state == MenuState.CHECKIN_OK
            || state == MenuState.GOODBYE
            || state == MenuState.END;
    }
}
