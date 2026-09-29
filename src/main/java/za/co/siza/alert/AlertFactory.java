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
 * Turns a finished USSD session into a domain Alert.
 * Translates USSD-specific knowledge (MenuState) into domain concepts.
 */
public final class AlertFactory {

    public static Alert fromUssdSession(UssdSession session, MenuState terminalState) {
        AlertType type = switch (terminalState) {
            case PANIC_SENT -> AlertType.PANIC;
            case CHECKIN_OK -> AlertType.CHECKIN;
            default -> AlertType.LOCATION_REPORT;
        };

        Severity severity = switch (terminalState) {
            case PANIC_SENT -> Severity.IMMEDIATE;
            case REPORT_CONFIRMED, LOCATION_UNKNOWN -> Severity.URGENT;
            default -> Severity.CONCERN;
        };

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

    private AlertFactory() {}
}
