package za.co.siza.ussd;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Turns a MenuState into the text the user sees on their phone.
 *
 * Menu text is loaded from /menus.json on the classpath at construction time.
 * If the file is missing or unreadable, falls back to hardcoded defaults so
 * tests and dev environments still work.
 *
 * USSD screens are capped at 182 characters on most South African networks.
 */
public final class MenuRenderer {

    public static final int MAX_SCREEN_LENGTH = 182;
    private static final String MENUS_RESOURCE = "/menus.json";

    private final Map<String, String> customMenus;

    public MenuRenderer() {
        this.customMenus = loadMenus();
    }

    public String render(MenuState state) {
        String text = customMenus.getOrDefault(state.name(), defaultText(state));
        if (text.length() > MAX_SCREEN_LENGTH) {
            text = text.substring(0, MAX_SCREEN_LENGTH);
        }
        return text;
    }

    private Map<String, String> loadMenus() {
        try (InputStream is = getClass().getResourceAsStream(MENUS_RESOURCE)) {
            if (is == null) {
                System.out.println("menus.json not found on classpath; using defaults.");
                return Map.of();
            }
            Map<String, String> parsed = new Gson().fromJson(
                new InputStreamReader(is, StandardCharsets.UTF_8),
                new TypeToken<Map<String, String>>() {}.getType()
            );
            return parsed == null ? Map.of() : parsed;
        } catch (Exception e) {
            System.err.println("Failed to load " + MENUS_RESOURCE
                + " (" + e.getMessage() + "); using defaults.");
            return Map.of();
        }
    }

    private String defaultText(MenuState state) {
        return switch (state) {
            case WELCOME -> "Siza\n"
                + "What do you need?\n"
                + "1. I need help now\n"
                + "2. Share my location\n"
                + "3. Walking check-in";

            case DANGER_CHECK -> "Are you in immediate danger?\n"
                + "1. Yes, someone is here\n"
                + "2. No, but I feel unsafe\n"
                + "3. I'm not sure";

            case PANIC_SENT -> "Alert sent to your contacts.\n"
                + "Help is being notified.\n"
                + "Stay on the line if you can.\n"
                + "Press any key to exit.";

            case ASK_LOCATION -> "Where are you?\n"
                + "1. I can type a street name\n"
                + "2. I don't know where I am";

            case LOCATION_TEXT -> "Type the street name or landmark:";

            case LOCATION_UNKNOWN -> "Alert sent with your last known area.\n"
                + "Your contacts are being notified.\n"
                + "Press any key to exit.";

            case REPORT_CONFIRMED -> "Report sent.\n"
                + "Your contacts will see it.\n"
                + "Press any key to exit.";

            case CHECKIN_MENU -> "Check-in\n"
                + "1. I'm okay\n"
                + "2. I need help";

            case CHECKIN_OK -> "Good. Your contacts know you're okay.\n"
                + "Press any key to exit.";

            case GOODBYE -> "Session ended.\n"
                + "Dial again if you need help.";

            case END -> "Session ended.";
        };
    }
}
