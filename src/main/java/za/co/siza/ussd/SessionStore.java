package za.co.siza.ussd;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory session store with time-to-live. Sessions expire after 3 minutes
 * of inactivity. Nothing here is written to disk.
 */
public final class SessionStore {

    private static final Duration TTL = Duration.ofMinutes(3);
    private final Map<String, UssdSession> sessions = new ConcurrentHashMap<>();

    public UssdSession getOrCreate(String sessionId, String phoneNumber) {
        evictExpired();
        return sessions.computeIfAbsent(sessionId,
            id -> new UssdSession(id, phoneNumber));
    }

    public void remove(String sessionId) {
        sessions.remove(sessionId);
    }

    public int size() {
        return sessions.size();
    }

    private void evictExpired() {
        Instant cutoff = Instant.now().minus(TTL);
        sessions.entrySet().removeIf(e ->
            e.getValue().lastActivityAt().isBefore(cutoff));
    }
}
