package za.co.siza.ussd;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves the JSON request format is understood by the parser the server uses.
 * If this test passes, a gateway sending JSON will work.
 */
class UssdRequestTest {

    private final Gson gson = new Gson();

    @Test void parsesFullJsonPayload() {
        String json = "{\"sessionId\":\"s1\",\"phoneNumber\":\"+27821234567\",\"text\":\"1\"}";
        UssdRequest req = gson.fromJson(json, UssdRequest.class);
        assertEquals("s1", req.sessionId());
        assertEquals("+27821234567", req.phoneNumber());
        assertEquals("1", req.text());
    }

    @Test void missingTextDefaultsToEmptyString() {
        String json = "{\"sessionId\":\"s1\",\"phoneNumber\":\"+27821234567\"}";
        UssdRequest req = gson.fromJson(json, UssdRequest.class);
        assertEquals("", req.text());
    }

    @Test void nullFieldsBecomeEmptyStrings() {
        UssdRequest req = new UssdRequest(null, null, null);
        assertEquals("", req.sessionId());
        assertEquals("", req.phoneNumber());
        assertEquals("", req.text());
    }
}
