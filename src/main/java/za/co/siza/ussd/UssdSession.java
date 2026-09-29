package za.co.siza.ussd;

import java.time.Instant;

/**
 * Ephemeral per-call state. Lives only as long as the USSD session.
 * Nothing here is persisted. If the session ends, this is gone.
 */
public final class UssdSession {

    private final String sessionId;
    private final String phoneNumber;
    private MenuState state;
    private String locationText;
    private final Instant createdAt;
    private Instant lastActivityAt;

    public UssdSession(String sessionId, String phoneNumber) {
        this.sessionId = sessionId;
        this.phoneNumber = phoneNumber;
        this.state = MenuState.WELCOME;
        this.createdAt = Instant.now();
        this.lastActivityAt = this.createdAt;
    }

    public String sessionId()       { return sessionId; }
    public String phoneNumber()     { return phoneNumber; }
    public MenuState state()        { return state; }
    public String locationText()    { return locationText; }
    public Instant createdAt()      { return createdAt; }
    public Instant lastActivityAt() { return lastActivityAt; }

    public void advanceTo(MenuState next) {
        this.state = next;
        this.lastActivityAt = Instant.now();
    }

    public void recordLocation(String text) {
        this.locationText = text;
        this.lastActivityAt = Instant.now();
    }
}
