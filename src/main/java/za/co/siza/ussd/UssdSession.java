package za.co.siza.ussd;

import java.time.Instant;

/**
 * Holds the temporary memory for one specific USSD session.
 * 
 * Think of this like a sticky note. When someone dials in, we create a 
 * sticky note with their phone number. As they move through the menu, 
 * we update the note. When they finish, we throw the note away. 
 * Nothing here is saved to a database.
 */
public final class UssdSession {

    // These never change. Once we know who is calling, it stays that way.
    private final String sessionId;
    private final String phoneNumber;
    private final Instant createdAt;
    
    // These change as the user moves through the menu.
    private MenuState state;
    private String locationText;
    private Instant lastActivityAt;

    /**
     * When a new session starts, we set the starting values.
     */
    public UssdSession(String sessionId, String phoneNumber) {
        this.sessionId = sessionId;
        this.phoneNumber = phoneNumber;
        
        // Everyone starts at the WELCOME screen.
        this.state = MenuState.WELCOME;
        
        // Record the time they started.
        this.createdAt = Instant.now();
        this.lastActivityAt = this.createdAt;
    }

    // These are "getter" methods. They just let other parts of the app 
    // look at the data without being able to change it directly.
    public String sessionId()       { return sessionId; }
    public String phoneNumber()     { return phoneNumber; }
    public MenuState state()        { return state; }
    public String locationText()    { return locationText; }
    public Instant createdAt()      { return createdAt; }
    public Instant lastActivityAt() { return lastActivityAt; }

    /**
     * Moves the user to a new screen.
     */
    public void advanceTo(MenuState next) {
        this.state = next;
        
        // Update the activity time so the SessionStore knows this session 
        // is still active and shouldn't be deleted yet.
        this.lastActivityAt = Instant.now();
    }

    /**
     * Saves the location the user typed.
     */
    public void recordLocation(String text) {
        this.locationText = text;
        
        // Update the activity time here too.
        this.lastActivityAt = Instant.now();
    }
}
