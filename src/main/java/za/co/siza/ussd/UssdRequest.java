package za.co.siza.ussd;

/**
 * Represents the data that comes in when a USSD gateway 
 * (or our curl test) sends a request to the server.
 * 
 * Depending on the gateway, this data might arrive as form fields 
 * or as a JSON object. Gson (our JSON library) uses this record 
 * to turn that incoming data into a Java object.
 */
public record UssdRequest(String sessionId, String phoneNumber, String text) {
    
    // This runs every time a new UssdRequest is created.
    public UssdRequest {
        // If the gateway sends a request and forgets a field, 
        // it arrives as 'null'. If we don't handle that here, the app 
        // will crash later when it tries to read the text. 
        // By turning nulls into empty strings (""), we make sure the 
        // rest of the app always has something safe to work with.
        if (sessionId == null) sessionId = "";
        if (phoneNumber == null) phoneNumber = "";
        if (text == null) text = "";
    }
}
