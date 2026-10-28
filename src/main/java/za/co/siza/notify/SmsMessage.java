package za.co.siza.notify;

/**
 * A simple object that holds an outgoing text message.
 * It just needs to know who it's going to, and what the message says.
 */
public record SmsMessage(String toPhoneNumber, String body) {
    
    // This runs every time a new SmsMessage is created.
    public SmsMessage {
        // We can't send a text if we don't know the phone number.
        if (toPhoneNumber == null || toPhoneNumber.isBlank())
            throw new IllegalArgumentException("toPhoneNumber required");
            
        // We also can't send an empty message.
        if (body == null || body.isBlank())
            throw new IllegalArgumentException("body required");
    }
}
