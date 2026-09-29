package za.co.siza.domain;

import java.time.Instant;

/**
 * The core value object. Every trigger - USSD, wearable, SMS inbound -
 * produces one of these. Downstream code never needs to know where it came from.
 */
public record Alert(
    String id,
    String phoneNumber,
    AlertType type,
    Severity severity,
    String locationText,
    Trigger trigger,
    Instant createdAt
) {
    public Alert {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id required");
        if (phoneNumber == null || phoneNumber.isBlank()) throw new IllegalArgumentException("phoneNumber required");
        if (type == null) throw new IllegalArgumentException("type required");
        if (severity == null) throw new IllegalArgumentException("severity required");
        if (trigger == null) throw new IllegalArgumentException("trigger required");
        if (createdAt == null) createdAt = Instant.now();
    }
}
