package za.co.siza.domain;

/**
 * Where the alert came from. 
 * This helps us track how people are actually using the service.
 */
public enum Trigger {
    
    USSD,         // The user dialled the USSD code on their phone.
    WEARABLE,     // The alert came from a wearable device (planned for later).
    SMS_INBOUND   // The user sent a text message to the system.
}
