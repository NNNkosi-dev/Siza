package za.co.siza.ussd;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * This class is responsible for turning the MenuState (like WELCOME or ASK_LOCATION) 
 * into the actual text that the user sees on their phone screen.
 * 
 * When the app starts, it tries to load the menu text from a file (menus.json). 
 * If that file is missing, or broken, it just falls back to the hardcoded 
 * text at the bottom of this file so the app never crashes.
 */
public final class MenuRenderer {

    // Here is our "magic number" pulled out into a constant.
    // Most South African networks cut off USSD text after 182 characters.
    // If the network limit changes, we only have to update this one line.
    public static final int MAX_SCREEN_LENGTH = 182;
    
    // The name of the file we try to load from the resources folder.
    private static final String MENUS_RESOURCE = "/menus.json";

    // This holds the menu text loaded from the JSON file. 
    // If the file didn't load, this will just be empty.
    private final Map<String, String> customMenus;

    public MenuRenderer() {
        // When we create a new MenuRenderer, it immediately tries to load the JSON file.
        this.customMenus = loadMenus();
    }

    public String render(MenuState state) {
        // Try to find the text in the loaded JSON file.
        // If it's not there, use the defaultText() method at the bottom of this file.
        String text = customMenus.getOrDefault(state.name(), defaultText(state));
        
        // Safety check: If the text is too long for a USSD screen, chop it off.
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
