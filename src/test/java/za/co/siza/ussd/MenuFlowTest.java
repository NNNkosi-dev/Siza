package za.co.siza.ussd;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MenuFlowTest {

    private final MenuFlow flow = new MenuFlow();

    @Test void welcomeOption1GoesToDangerCheck() {
        assertEquals(MenuState.DANGER_CHECK, flow.next(MenuState.WELCOME, "1"));
    }

    @Test void welcomeOption2GoesToAskLocation() {
        assertEquals(MenuState.ASK_LOCATION, flow.next(MenuState.WELCOME, "2"));
    }

    @Test void welcomeOption3GoesToCheckinMenu() {
        assertEquals(MenuState.CHECKIN_MENU, flow.next(MenuState.WELCOME, "3"));
    }

    @Test void invalidWelcomeInputStaysOnWelcome() {
        assertEquals(MenuState.WELCOME, flow.next(MenuState.WELCOME, "9"));
        assertEquals(MenuState.WELCOME, flow.next(MenuState.WELCOME, ""));
        assertEquals(MenuState.WELCOME, flow.next(MenuState.WELCOME, null));
    }

    @Test void dangerCheckYesGoesToPanicSent() {
        assertEquals(MenuState.PANIC_SENT, flow.next(MenuState.DANGER_CHECK, "1"));
    }

    @Test void dangerCheckNotSureGoesToAskLocation() {
        assertEquals(MenuState.ASK_LOCATION, flow.next(MenuState.DANGER_CHECK, "3"));
    }

    @Test void askLocationOption1GoesToFreeText() {
        assertEquals(MenuState.LOCATION_TEXT, flow.next(MenuState.ASK_LOCATION, "1"));
        assertTrue(flow.expectsFreeText(MenuState.LOCATION_TEXT));
    }

    @Test void askLocationOption2GoesToUnknown() {
        assertEquals(MenuState.LOCATION_UNKNOWN, flow.next(MenuState.ASK_LOCATION, "2"));
    }

    @Test void freeTextLocationGoesToReportConfirmed() {
        assertEquals(MenuState.REPORT_CONFIRMED,
            flow.next(MenuState.LOCATION_TEXT, "Corner Main and Oak"));
    }

    @Test void panicSentIsTerminalImmediately() {
        assertTrue(flow.isTerminal(MenuState.PANIC_SENT));
    }

    @Test void reportConfirmedIsTerminalImmediately() {
        assertTrue(flow.isTerminal(MenuState.REPORT_CONFIRMED));
    }

    @Test void welcomeIsNotTerminal() {
        assertFalse(flow.isTerminal(MenuState.WELCOME));
        assertFalse(flow.isTerminal(MenuState.DANGER_CHECK));
    }

    @Test void checkinOkIsTerminalPath() {
        assertEquals(MenuState.CHECKIN_OK, flow.next(MenuState.CHECKIN_MENU, "1"));
        assertTrue(flow.isTerminal(MenuState.CHECKIN_OK));
    }
}
