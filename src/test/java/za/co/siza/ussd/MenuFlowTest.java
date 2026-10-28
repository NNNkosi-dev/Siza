package za.co.siza.ussd;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the MenuFlow class.
 * 
 * These tests make sure the state machine moves users to the correct 
 * screen. If we ever change the menu text or add new options, these 
 * tests will catch us if we accidentally break the flow.
 */
class MenuFlowTest {

    // We create one MenuFlow object to use across all the tests.
    private final MenuFlow flow = new MenuFlow();

    @Test 
    void welcomeOption1GoesToDangerCheck() {
        // If the user presses '1' on the WELCOME screen, they should see the danger check.
        assertEquals(MenuState.DANGER_CHECK, flow.next(MenuState.WELCOME, "1"));
    }

    @Test 
    void welcomeOption2GoesToAskLocation() {
        // Pressing '2' should take them to the location menu.
        assertEquals(MenuState.ASK_LOCATION, flow.next(MenuState.WELCOME, "2"));
    }

    @Test 
    void welcomeOption3GoesToCheckinMenu() {
        // Pressing '3' should take them to the check-in menu.
        assertEquals(MenuState.CHECKIN_MENU, flow.next(MenuState.WELCOME, "3"));
    }

    @Test 
    void invalidWelcomeInputStaysOnWelcome() {
        // If they type a number that isn't an option (like '9'), 
        // or if they just press Enter (empty string), or if the input is null, 
        // they should just stay on the WELCOME screen instead of crashing.
        assertEquals(MenuState.WELCOME, flow.next(MenuState.WELCOME, "9"));
        assertEquals(MenuState.WELCOME, flow.next(MenuState.WELCOME, ""));
        assertEquals(MenuState.WELCOME, flow.next(MenuState.WELCOME, null));
    }

    @Test 
    void dangerCheckYesGoesToPanicSent() {
        // If they say "Yes, someone is here" (1), the panic alert should fire.
        assertEquals(MenuState.PANIC_SENT, flow.next(MenuState.DANGER_CHECK, "1"));
    }

    @Test 
    void dangerCheckNotSureGoesToAskLocation() {
        // If they aren't sure (3), we still treat it seriously and ask for their location.
        assertEquals(MenuState.ASK_LOCATION, flow.next(MenuState.DANGER_CHECK, "3"));
    }

    @Test 
    void askLocationOption1GoesToFreeText() {
        // If they choose to type a street name (1), we move to the free text screen.
        assertEquals(MenuState.LOCATION_TEXT, flow.next(MenuState.ASK_LOCATION, "1"));
        
        // And we must make sure the system knows this screen expects text, 
        // not a menu number.
        assertTrue(flow.expectsFreeText(MenuState.LOCATION_TEXT));
    }

    @Test 
    void askLocationOption2GoesToUnknown() {
        // If they don't know where they are (2), we move to the unknown screen.
        assertEquals(MenuState.LOCATION_UNKNOWN, flow.next(MenuState.ASK_LOCATION, "2"));
    }

    @Test 
    void freeTextLocationGoesToReportConfirmed() {
        // If they type "Corner Main and Oak", the system should accept it 
        // as text and move them to the confirmation screen.
        assertEquals(MenuState.REPORT_CONFIRMED,
            flow.next(MenuState.LOCATION_TEXT, "Corner Main and Oak"));
    }

    @Test 
    void panicSentIsTerminalImmediately() {
        // A panic alert should immediately end the session so the SMS fires.
        assertTrue(flow.isTerminal(MenuState.PANIC_SENT));
    }

    @Test 
    void reportConfirmedIsTerminalImmediately() {
        // A confirmed report should also end the session.
        assertTrue(flow.isTerminal(MenuState.REPORT_CONFIRMED));
    }

    @Test 
    void welcomeIsNotTerminal() {
        // The first screens should NOT end the session.
        assertFalse(flow.isTerminal(MenuState.WELCOME));
        assertFalse(flow.isTerminal(MenuState.DANGER_CHECK));
    }

    @Test 
    void checkinOkIsTerminalPath() {
        // Pressing '1' on the check-in menu means they are okay, 
        // which should end the session.
        assertEquals(MenuState.CHECKIN_OK, flow.next(MenuState.CHECKIN_MENU, "1"));
        assertTrue(flow.isTerminal(MenuState.CHECKIN_OK));
    }
}
