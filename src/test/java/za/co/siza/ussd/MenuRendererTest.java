package za.co.siza.ussd;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the MenuRenderer class.
 * 
 * These tests make sure the text we send to the user is safe and valid. 
 * If a menu is too long, the network will cut it off. If it's empty, 
 * the user won't know what to do. These tests stop that from happening.
 */
class MenuRendererTest {

    // We create one renderer to use across all the tests.
    private final MenuRenderer renderer = new MenuRenderer();

    @Test 
    void everyScreenFitsWithinUssdLimit() {
        // Loop through every single screen we have.
        for (MenuState state : MenuState.values()) {
            String text = renderer.render(state);
            
            // Assert that the text is not longer than our MAX_SCREEN_LENGTH (182).
            // If it is, the test will fail and tell us exactly which screen is too long.
            assertTrue(text.length() <= MenuRenderer.MAX_SCREEN_LENGTH,
                state + " exceeds " + MenuRenderer.MAX_SCREEN_LENGTH
                    + " chars (was " + text.length() + ")");
        }
    }

    @Test 
    void welcomeMentionsAllThreeOptions() {
        // Get the text for the first screen.
        String text = renderer.render(MenuState.WELCOME);
        
        // Make sure the user actually sees the options: 1, 2, and 3.
        // If someone accidentally deletes an option from the text, this test catches it.
        assertTrue(text.contains("1."));
        assertTrue(text.contains("2."));
        assertTrue(text.contains("3."));
    }

    @Test 
    void rendererNeverReturnsNullOrEmpty() {
        // Loop through every screen again.
        for (MenuState state : MenuState.values()) {
            String text = renderer.render(state);
            
            // Make sure the text is not null...
            assertNotNull(text);
            
            // ...and make sure it's not blank (like an empty string or just spaces).
            // A blank screen would leave the user stuck.
            assertFalse(text.isBlank(), state + " rendered blank");
        }
    }
}
