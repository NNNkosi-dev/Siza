package za.co.siza.ussd;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MenuRendererTest {

    private final MenuRenderer renderer = new MenuRenderer();

    @Test void everyScreenFitsWithinUssdLimit() {
        for (MenuState state : MenuState.values()) {
            String text = renderer.render(state);
            assertTrue(text.length() <= MenuRenderer.MAX_SCREEN_LENGTH,
                state + " exceeds " + MenuRenderer.MAX_SCREEN_LENGTH
                    + " chars (was " + text.length() + ")");
        }
    }

    @Test void welcomeMentionsAllThreeOptions() {
        String text = renderer.render(MenuState.WELCOME);
        assertTrue(text.contains("1."));
        assertTrue(text.contains("2."));
        assertTrue(text.contains("3."));
    }

    @Test void rendererNeverReturnsNullOrEmpty() {
        for (MenuState state : MenuState.values()) {
            String text = renderer.render(state);
            assertNotNull(text);
            assertFalse(text.isBlank(), state + " rendered blank");
        }
    }
}
