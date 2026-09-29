package za.co.siza.ussd;

/**
 * The shape of an incoming USSD request. Gateways send this either as
 * form-encoded fields (application/x-www-form-urlencoded) or as JSON.
 * Gson maps JSON fields by name to these record components.
 */
public record UssdRequest(String sessionId, String phoneNumber, String text) {
    public UssdRequest {
        if (sessionId == null) sessionId = "";
        if (phoneNumber == null) phoneNumber = "";
        if (text == null) text = "";
    }
}
