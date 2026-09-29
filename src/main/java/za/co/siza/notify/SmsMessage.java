package za.co.siza.notify;

public record SmsMessage(String toPhoneNumber, String body) {
    public SmsMessage {
        if (toPhoneNumber == null || toPhoneNumber.isBlank())
            throw new IllegalArgumentException("toPhoneNumber required");
        if (body == null || body.isBlank())
            throw new IllegalArgumentException("body required");
    }
}
