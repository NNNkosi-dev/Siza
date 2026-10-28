package za.co.siza.alert;

import za.co.siza.domain.Alert;
import za.co.siza.domain.AlertType;
import za.co.siza.domain.Severity;
import za.co.siza.notify.SmsMessage;
import za.co.siza.notify.SmsSender;

/**
 * Takes an Alert and sends out the SMS notification.
 * 
 * It doesn't create its own SmsSender. Instead, we pass one in 
 * when we create the AlertService. This makes it easy to swap 
 * between the mock sender (for testing) and a real SMS provider later.
 */
public final class AlertService {

    private final SmsSender sender;

    public AlertService(SmsSender sender) {
        this.sender = sender;
    }

    /**
     * Builds the message and passes it to the sender.
     */
    public void dispatch(Alert alert) {
        String body = buildMessage(alert);
        sender.send(new SmsMessage(alert.phoneNumber(), body));
    }

    /**
     * Figures out what the SMS should say based on the type of alert.
     */
    private String buildMessage(Alert alert) {
        // If it's a panic or an immediate danger, use serious wording.
        if (alert.type() == AlertType.PANIC
            || alert.severity() == Severity.IMMEDIATE) {
            return "Siza: your alert was received. " + locationClause(alert);
        }
        
        // If they are just checking in to say they are okay, use calmer wording.
        if (alert.type() == AlertType.CHECKIN) {
            return "Siza: check-in recorded. " + locationClause(alert);
        }
        
        // Any other type of alert is treated as a location report.
        return "Siza: report recorded. " + locationClause(alert);
    }

    /**
     * Handles the location part of the SMS.
     * If the user didn't type a location, we still need to say something, 
     * so we just say "last known area".
     */
    private String locationClause(Alert alert) {
        if (alert.locationText() == null || alert.locationText().isBlank()) {
            return "Location: last known area.";
        }
        return "Location: " + alert.locationText() + ".";
    }
}
