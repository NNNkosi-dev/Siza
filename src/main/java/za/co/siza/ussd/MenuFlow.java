package za.co.siza.ussd;

/**
 * The USSD state machine. Given a current state and the user's input,
 * returns the next state. Pure logic. No HTTP, no I/O.
 */
public final class MenuFlow {

    public MenuState next(MenuState current, String input) {
        if (input == null) input = "";
        input = input.trim();

        return switch (current) {
            case WELCOME -> switch (input) {
                case "1" -> MenuState.DANGER_CHECK;
                case "2" -> MenuState.ASK_LOCATION;
                case "3" -> MenuState.CHECKIN_MENU;
                default  -> MenuState.WELCOME;
            };
            case DANGER_CHECK -> switch (input) {
                case "1" -> MenuState.PANIC_SENT;
                case "2" -> MenuState.ASK_LOCATION;
                case "3" -> MenuState.ASK_LOCATION;
                default  -> MenuState.DANGER_CHECK;
            };
            case ASK_LOCATION -> switch (input) {
                case "1" -> MenuState.LOCATION_TEXT;
                case "2" -> MenuState.LOCATION_UNKNOWN;
                default  -> MenuState.ASK_LOCATION;
            };
            case LOCATION_TEXT -> MenuState.REPORT_CONFIRMED;
            case LOCATION_UNKNOWN -> MenuState.REPORT_CONFIRMED;
            case CHECKIN_MENU -> switch (input) {
                case "1" -> MenuState.CHECKIN_OK;
                case "2" -> MenuState.ASK_LOCATION;
                default  -> MenuState.CHECKIN_MENU;
            };
            case CHECKIN_OK -> MenuState.GOODBYE;
            case PANIC_SENT, REPORT_CONFIRMED, GOODBYE, END -> MenuState.END;
        };
    }

    public boolean expectsFreeText(MenuState state) {
        return state == MenuState.LOCATION_TEXT;
    }

    /**
     * A state is terminal when reaching it means the session is finished
     * and any resulting alert should be dispatched.
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
