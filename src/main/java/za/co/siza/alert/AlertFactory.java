package za.co.siza.alert;

import za.co.siza.domain.Alert;
import za.co.siza.domain.AlertType;
import za.co.siza.domain.Severity;
import za.co.siza.domain.Trigger;
import za.co.siza.ussd.MenuState;
import za.co.siza.ussd.UssdSession;

import java.time.Instant;
import java.util.UUID;

/**
 * Takes a finished USSD session and turns it into an Alert object.
 * 
 * The USSD side only knows about menus and screens. The rest of the app 
 * (and the SMS sender) doesn't care about menus, it only cares about Alerts. 
 * This class does the translation between the two.
 */
public final class AlertFactory {

    /**
     * Figures out what kind of alert to make based on the last screen 
     * the user saw.
     */
    public static Alert fromUssdSession(UssdSession session, MenuState terminalState) {
        
        // If the user ended on the panic screen, it's a PANIC alert.
        // If they checked in, it's a CHECKIN.
        // Anything else (like sharing a location) is a LOCATION_REPORT.
        AlertType type = switch (terminalState) {
            case PANIC_SENT -> AlertType.PANIC;
            case CHECKIN_OK -> AlertType.CHECKIN;
            default -> AlertType.LOCATION_REPORT;
        };

        // How serious is this? 
        // Panic is always IMMEDIATE. 
        // Sharing a location (even an unknown one) is URGENT.
        // Just checking in is logged as a CONCERN.
        Severity severity = switch (terminalState) {
            case PANIC_SENT -> Severity.IMMEDIATE;
            case REPORT_CONFIRMED, LOCATION_UNKNOWN -> Severity.URGENT;
            default -> Severity.CONCERN;
        };

        // Create the actual Alert object.
        // We generate a random ID, grab the details from the session, 
        // and mark it as coming from USSD.
        return new Alert(
            UUID.randomUUID().toString(),
            session.phoneNumber(),
            type,
            severity,
            session.locationText(),
            Trigger.USSD,
            Instant.now()
        );
    }

    // Made private so nobody can accidentally create a `new AlertFactory()`.
    // It only has static methods, so it doesn't need to be instantiated.
    private AlertFactory() {}
}
