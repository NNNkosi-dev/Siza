package za.co.siza.ussd;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import za.co.siza.alert.AlertFactory;
import za.co.siza.alert.AlertService;
import za.co.siza.domain.Alert;
import za.co.siza.notify.MockSmsSender;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the web server. It listens for incoming HTTP requests, 
 * translates them, passes them to the menu logic, and sends the 
 * result back.
 * 
 * It accepts two types of requests:
 * 1. Form-encoded (which is what real USSD gateways send).
 * 2. JSON (which is easier to test with locally).
 * 
 * It replies with plain text: the next screen the user should see.
 */
public final class UssdServer {

    // The port the server runs on.
    private static final int PORT = 8080;
    
    // Standard HTTP Status Codes so we don't use magic numbers.
    private static final int HTTP_OK = 200;
    private static final int HTTP_BAD_REQUEST = 400;
    private static final int HTTP_METHOD_NOT_ALLOWED = 405;
    
    // We create one Gson object and reuse it, rather than making a new one 
    // every time a request comes in.
    private static final Gson GSON = new Gson();

    // These are the "tools" the server needs to do its job.
    private final SessionStore sessions = new SessionStore();
    private final MenuFlow flow = new MenuFlow();
    private final MenuRenderer renderer = new MenuRenderer();
    
    // Right now, we are using the MockSmsSender so it just prints to the console.
    private final AlertService alertService = new AlertService(new MockSmsSender());

    /**
     * Starts the server and tells it to listen on the configured port.
     */
    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        // When someone goes to /ussd, run the handleUssd method.
        server.createContext("/ussd", this::handleUssd);
        server.setExecutor(null);
        server.start();

        // This makes sure the server shuts down cleanly if we press Ctrl+C.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down Siza USSD server...");
            server.stop(0);
        }));

        System.out.println("Siza USSD server listening on http://localhost:" + PORT);
        System.out.println("POST /ussd with form-encoded or JSON body.");
    }

    /**
     * The main brain of the server. Handles every incoming request.
     */
    private void handleUssd(HttpExchange exchange) throws IOException {
        // 1. Check if they are using POST. If not, send an error.
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            respond(exchange, HTTP_METHOD_NOT_ALLOWED, "Method not allowed");
            return;
        }

        // 2. Read the body and the content type (JSON or form).
        String body = readBody(exchange);
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        
        // 3. Turn the body into our UssdRequest object.
        UssdRequest request = parseRequest(body, contentType);

        // 4. Make sure we actually got a session ID and phone number.
        if (request.sessionId().isBlank() || request.phoneNumber().isBlank()) {
            respond(exchange, HTTP_BAD_REQUEST, "Missing sessionId or phoneNumber");
            return;
        }

        // 5. Find their session (or create a new one if they just dialled in).
        UssdSession session = sessions.getOrCreate(request.sessionId(), request.phoneNumber());
        MenuState current = session.state();

        // 6. Figure out what they typed and move to the next screen.
        MenuState next;
        if (flow.expectsFreeText(current)) {
            // If they are typing a location, save that text to the session.
            session.recordLocation(request.text());
            next = flow.next(current, request.text());
        } else {
            // If they are just pressing a menu number, just move to the next screen.
            next = flow.next(current, request.text());
        }

        // 7. Update the session so it remembers the new screen.
        session.advanceTo(next);

        // 8. If this was the last screen, trigger the alert and delete the session.
        if (flow.isTerminal(next)) {
            onSessionEnd(session, next);
            sessions.remove(request.sessionId());
        }

        // 9. Send the new screen back to the user.
        respond(exchange, HTTP_OK, renderer.render(next));
    }

    /**
     * Looks at the Content-Type header and figures out how to read the body.
     */
    private UssdRequest parseRequest(String body, String contentType) {
        // If it's JSON, use Gson to parse it.
        if (contentType != null
            && contentType.toLowerCase().contains("application/json")) {
            try {
                UssdRequest parsed = GSON.fromJson(body, UssdRequest.class);
                // If the JSON was empty, return an empty request instead of null.
                return parsed != null ? parsed : new UssdRequest("", "", "");
            } catch (Exception e) {
                // If the JSON was broken, print the error and return empty.
                System.err.println("Bad JSON in /ussd request: " + e.getMessage());
                return new UssdRequest("", "", "");
            }
        }
        
        // If it's not JSON, assume it's form-encoded and use our custom parser.
        Map<String, String> params = parseForm(body);
        return new UssdRequest(
            params.getOrDefault("sessionId", ""),
            params.getOrDefault("phoneNumber", ""),
            params.getOrDefault("text", "")
        );
    }

    /**
     * When the user reaches the end of the menu, we create an Alert, 
     * send it to the AlertService, and print a log line.
     */
    private void onSessionEnd(UssdSession session, MenuState terminalState) {
        System.out.println("[SESSION END] phone=" + session.phoneNumber()
            + " location=" + session.locationText()
            + " terminalState=" + terminalState);
            
        Alert alert = AlertFactory.fromUssdSession(session, terminalState);
        alertService.dispatch(alert);
    }

    /**
     * Reads the raw text from the request body.
     */
    private String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    /**
     * A simple custom parser for form-encoded data (like "name=John&age=30").
     */
    private Map<String, String> parseForm(String body) {
        Map<String, String> out = new HashMap<>();
        if (body == null || body.isEmpty()) return out;
        
        // Split the string by the '&' character, then split each pair by '='.
        for (String pair : body.split("&")) {
            int i = pair.indexOf('=');
            if (i > 0) {
                out.put(decode(pair.substring(0, i)), decode(pair.substring(i + 1)));
            }
        }
        return out;
    }

    /**
     * Turns URL-encoded characters back into normal text.
     */
    private String decode(String s) {
        try {
            return java.net.URLDecoder.decode(s, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }

    /**
     * Sends the final HTTP response back to the client.
     */
    private void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * The starting point of the whole app.
     */
    public static void main(String[] args) throws IOException {
        new UssdServer().start();
    }
}
