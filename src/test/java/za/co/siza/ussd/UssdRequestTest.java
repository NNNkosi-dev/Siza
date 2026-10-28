package za.co.siza.ussd;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the UssdRequest class.
 * 
 * These tests make sure the app can safely read incoming JSON data. 
 * If a USSD gateway sends a message that is missing a field, or sends 
 * a null value, the app shouldn't crash. It should just treat the 
 * missing data as an empty string.
 */
class UssdRequestTest {

    // We need a Gson object to turn JSON text into a Java object.
    private final Gson gson = new Gson();

    @Test 
    void parsesFullJsonPayload() {
        // A perfect JSON request with all three fields.
        String json = "{\"sessionId\":\"s1\",\"phoneNumber\":\"+27821234567\",\"text\":\"1\"}";
        
        // Convert it to a Java object.
        UssdRequest req = gson.fromJson(json, UssdRequest.class);
        
        // Check that every field arrived correctly.
        assertEquals("s1", req.sessionId());
        assertEquals("+27821234567", req.phoneNumber());
        assertEquals("1", req.text());
    }

    @Test 
    void missingTextDefaultsToEmptyString() {
        // What if the gateway forgets to send the "text" field?
        // (This happens sometimes on the very first screen when the user 
        // just dials the code but hasn't typed anything yet).
        String json = "{\"sessionId\":\"s1\",\"phoneNumber\":\"+27821234567\"}";
        
        UssdRequest req = gson.fromJson(json, UssdRequest.class);
        
        // The text should be an empty string (""), not null.
        assertEquals("", req.text());
    }

    @Test 
    void nullFieldsBecomeEmptyStrings() {
        // What if someone accidentally passes null directly in the code?
        // Our UssdRequest constructor should catch it and turn it into "".
        UssdRequest req = new UssdRequest(null, null, null);
        
        assertEquals("", req.sessionId());
        assertEquals("", req.phoneNumber());
        assertEquals("", req.text());
    }
}
