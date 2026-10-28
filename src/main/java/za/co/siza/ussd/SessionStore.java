package za.co.siza.ussd;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps track of active USSD sessions in memory.
 * 
 * A user might dial in, type something, and take a minute to type the next thing. 
 * We need to remember where they were in the menu. But we can't keep them in memory 
 * forever, so this class automatically deletes any session that has been inactive 
 * for 3 minutes. Nothing here is saved to a database.
 */
public final class SessionStore {

    // Here is our magic number pulled out into a constant.
    // Most USSD gateways drop the connection after 2 to 3 minutes of inactivity.
    private static final Duration TTL = Duration.ofMinutes(3);
    
    // A thread-safe map to store sessions. "ConcurrentHashMap" means if 
    // two users send a request at the exact same millisecond, it won't crash.
    private final Map<String, UssdSession> sessions = new ConcurrentHashMap<>();

    /**
     * Finds an existing session, or creates a new one if this is their first message.
     */
    public UssdSession getOrCreate(String sessionId, String phoneNumber) {
        // Before we do anything, let's clean up any old, expired sessions.
        evictExpired();
        
        // Look for the session. If it doesn't exist, create a new one.
        return sessions.computeIfAbsent(sessionId,
            id -> new UssdSession(id, phoneNumber));
    }

    /**
     * Deletes a session. We call this when the user reaches the end of the menu.
     */
    public void remove(String sessionId) {
        sessions.remove(sessionId);
    }

    /**
     * Tells us how many active sessions there are (mostly for testing).
     */
    public int size() {
        return sessions.size();
    }

    /**
     * Cleans up the map by finding any session that hasn't been used 
     * in the last 3 minutes and deleting it. This stops the app from 
     * slowly using up all the computer's memory over time.
     */
    private void evictExpired() {
        // Figure out what time it was 3 minutes ago.
        Instant cutoff = Instant.now().minus(TTL);
        
        // Remove any session where the last activity was BEFORE the cutoff time.
        sessions.entrySet().removeIf(e ->
            e.getValue().lastActivityAt().isBefore(cutoff));
    }
}
