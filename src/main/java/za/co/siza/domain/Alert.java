package za.co.siza.domain;

import java.time.Instant;

/**
 * This is the main "Alert" object. 
 * Whether the alert comes from USSD, an SMS, or a future wearable, 
 * it all gets turned into this exact shape so the rest of the app 
 * knows how to handle it.
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
    // This is a "compact constructor" (a feature of Java records).
    // It runs every time someone creates a new Alert.
    public Alert {
        // These checks stop bad data from getting into the system. 
        // If an Alert is missing an ID, phone number, or type, 
        // the app will crash right here instead of failing silently later.
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id required");
        if (phoneNumber == null || phoneNumber.isBlank()) throw new IllegalArgumentException("phoneNumber required");
        if (type == null) throw new IllegalArgumentException("type required");
        if (severity == null) throw new IllegalArgumentException("severity required");
        if (trigger == null) throw new IllegalArgumentException("trigger required");
        
        // If no time was given, just use the current time.
        if (createdAt == null) createdAt = Instant.now();
    }
}
