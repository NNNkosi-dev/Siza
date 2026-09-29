package za.co.siza.alert;

import za.co.siza.domain.Alert;
import za.co.siza.domain.AlertType;
import za.co.siza.domain.Severity;
import za.co.siza.notify.SmsMessage;
import za.co.siza.notify.SmsSender;

/**
 * Takes a domain Alert and dispatches SMS notifications.
 * The SmsSender is injected so the transport can be swapped (mock, real gateway, etc.).
 */
public final class AlertService {

    private final SmsSender sender;

    public AlertService(SmsSender sender) {
        this.sender = sender;
    }

    public void dispatch(Alert alert) {
        String body = buildMessage(alert);
        sender.send(new SmsMessage(alert.phoneNumber(), body));
    }

    private String buildMessage(Alert alert) {
        if (alert.type() == AlertType.PANIC
            || alert.severity() == Severity.IMMEDIATE) {
            return "Siza: your alert was received. " + locationClause(alert);
        }
        if (alert.type() == AlertType.CHECKIN) {
            return "Siza: check-in recorded. " + locationClause(alert);
        }
        return "Siza: report recorded. " + locationClause(alert);
    }

    private String locationClause(Alert alert) {
        if (alert.locationText() == null || alert.locationText().isBlank()) {
            return "Location: last known area.";
        }
        return "Location: " + alert.locationText() + ".";
    }
}
